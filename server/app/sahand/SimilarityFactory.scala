package sahand

import java.io.File
import javax.inject._

import com.medallia.word2vec.Word2VecModel
import edu.illinois.cs.cogcomp.entitySimilarity.compare.WebWrapper
import edu.illinois.cs.cogcomp.sim.{PhraseSim, WNSimSimple}
import github.sahand.SimilarityNames
import org.cogcomp.Datastore
import play.api.libs.json.Json
import play.api.mvc._

// this is where we load the load the lazy implementation of the similarity metrics
object SimilarityFactory {
  val ds = new Datastore("http://smaug.cs.illinois.edu:8080")
  lazy val w2v = {
    val w2vFile = ds.getFile("org.cogcomp.embeddings", "GoogleNews-vectors-negative300-length=200000.bin", 1.0)
    new Word2VecSimilarity(w2vFile)
  }
  lazy val wnSim = new WNSimSimple()
  lazy val neSim = new WebWrapper()
  lazy val phraseSim: PhraseSim = PhraseSim.getInstance()
}

/** Various options for computing similarity */
sealed trait SimilarityTrait {

  // turn a one-sided score into a symmetric one
  protected def getSymmetricScore(text1: String, text2: String, context1: Option[String], context2: Option[String],
                                  scoringFunction: (String, String, Option[String], Option[String]) => Double): Double = {
    (scoringFunction(text1, text2, context1, context2) + scoringFunction(text2, text1, context1, context2)) / 2d
  }

  protected def getSymmetricScore(text1: String, text2: String, scoringFunction: (String, String) => Double): Double = {
    (scoringFunction(text1, text2) + scoringFunction(text2, text1)) / 2d
  }

  // take the max of scores across various hypothesis strings
  protected def getMaxScore(text1: String, text2Seq: Seq[String],
                            scoringFunction: (String, String) => Double): Double = {
    text2Seq.map(scoringFunction(text1, _)).max
  }

  protected def getMaxScore(text1: String, text2Seq: Seq[String], context1Opt: Option[String],
                            context2OptSeq: Seq[Option[String]],
                            scoringFunction: (String, String, Option[String], Option[String]) => Double): Double = {
    text2Seq.zip(context2OptSeq).map {
      case (text2, context2Opt) =>
        scoringFunction(text1, text2, context1Opt, context2Opt)
    }.max
  }
}

// cosine distance between two pieces of text (inherently symmetric)
class Word2VecSimilarity(word2vecFile: File) extends SimilarityTrait {
  private val w2vModel = Word2VecModel.fromBinFile(word2vecFile)
  private val w2vNoMatchStr = "</s>" // string used by word2vec when there is no match
  private def getWord2VecScore(text1: String, text2: String): Double = {
    val text1Modified = if (w2vModel.forSearch().contains(text1)) text1 else w2vNoMatchStr
    val text2Modified = if (w2vModel.forSearch().contains(text2)) text2 else w2vNoMatchStr
    w2vModel.forSearch().cosineDistance(text1Modified, text2Modified)
  }
  def getScore(text1: String, text2: String): Double = getWord2VecScore(text1, text2)
}


case class SimilarityResponse(
                   text1: String = "" ,
                   text2: String = "",
                   similarityType: String = "",
                   score: Double = -1.0,
                   log: String = ""
                   )

case class EmbeddingResponse(
                               text: String = "" ,
                               embeddingType: String = "",
                               embeddingResult: String = "",
                               log: String = ""
                             )

object ResultJson {
  import play.api.libs.json._

  implicit val simResponseWrites = new Writes[SimilarityResponse] {
    def writes(r: SimilarityResponse) = Json.obj(
      "text1" -> r.text1,
      "text2" -> r.text2,
      "similarityType" -> r.similarityType,
      "score" -> r.score,
      "log" -> r.log
    )
  }

  implicit val embResponseWrites = new Writes[EmbeddingResponse] {
    def writes(r: EmbeddingResponse) = Json.obj(
      "text" -> r.text,
      "embeddingType" -> r.embeddingType,
      "embeddingResult" -> r.embeddingResult,
      "log" -> r.log
    )
  }
}
@Singleton
class SimilarityFactory @Inject() extends Controller {
  def index = Action { implicit request =>
    Ok("")
  }

  import ResultJson._
  import play.api.libs.json._

  def getSimilarity(str1: String, str2: String, metrics: String) = Action { implicit request =>
    val metricsList = metrics.split(",").map(_.trim)

    val listOfResponse = metricsList.map {
      case SimilarityNames.word2vec => SimilarityResponse(str1, str2, SimilarityNames.word2vec, SimilarityFactory.w2v.getScore(str1, str2))
      case SimilarityNames.phrasesim =>
        val result = SimilarityFactory.phraseSim.compare(str1.split(" "), str2.split(" "))
        val score = if(result.score.isNaN) -100.0 else result.score
        SimilarityResponse(str1, str2, SimilarityNames.phrasesim, score, result.reason)
      case SimilarityNames.wnsim =>
        val response = SimilarityFactory.wnSim.compare(str1, str2)
        SimilarityResponse(str1, str2, SimilarityNames.wnsim, response.score, log = response.reason)
      case SimilarityNames.nesim =>
        SimilarityFactory.neSim.compare(str1, str2)
        SimilarityResponse(str1, str2, SimilarityNames.wnsim, SimilarityFactory.neSim.getScore, log = SimilarityFactory.neSim.getReason)
      case _ => SimilarityResponse(str1, str2, "", SimilarityFactory.w2v.getScore(str1, str2), log = "Similarity type invalid . . . ")
    }

    // in the output return json file result of similarity evaluations
    Ok(Json.toJson(listOfResponse).toString())
  }

  def getEmbedding(str: String, embedding: String) = Action { implicit request =>
    Ok(Json.toJson(Seq(EmbeddingResponse())).toString())
  }
}