package github.sahand

import java.net.URLEncoder

import org.mapdb.{DBMaker, Serializer}

import scala.io.Source

/**
  * This client talks with the Sahand server
  * @param endPoint the url of the Sahand server. Example is: "http://localhost:9001/"
  */
class SahandClient(endPoint: String, dbFile: String = "sahandClientDB.db") {

  private val dataset = "sahand"
  private var db = DBMaker.fileDB(dbFile).closeOnJvmShutdown().transactionEnable().make()

  def createKey(text1: String, text2: String, metric: String): String = { text1 + text2 + metric }

  def checkDB(key: String): Option[Double] = {
    val dbFile = db.hashMap(dataset, Serializer.STRING, Serializer.DOUBLE).createOrOpen()
    if( dbFile.containsKey(key) ) {
      Some(dbFile.get(key))
    }
    else {
      None
    }
  }

  def saveOnDB(key: String, score: Double): Unit = {
    val dbFile = db.hashMap(dataset, Serializer.STRING, Serializer.DOUBLE).createOrOpen()
    dbFile.put(key, score)
    db.commit()
  }

  def getScore(text1: String, text2: String, metric: String): Double = {
    val key = createKey(text1, text2, metric)
    val output = checkDB(key)
    output.getOrElse {
      val json = get(creatURL(text1, text2, metric))
      import play.api.libs.json.Json
      import play.api.libs.json._
      val parsedJson = Json.parse(json)
      val headResult = parsedJson.as[JsArray].value.head
      val score = (headResult \ "score").validate[Double].get
      saveOnDB(key, score)
      //println("Saving on db . . . ")
      score
    }
  }

  private def creatURL(text1: String, text2: String, metrics: String): String = {
    endPoint + "similarity?str1="+ URLEncoder.encode(text1, "UTF-8") + "&str2=" + URLEncoder.encode(text2, "UTF-8") +
      "&metrics=" + URLEncoder.encode(metrics, "UTF-8")
  }

  @throws(classOf[java.io.IOException])
  private def get(url: String) = Source.fromURL(url).mkString

  /**
    * MapDB requires the database to be closed at the end of operations. This is usually handled by the
    * {@code closeOnJvmShutdown()} snippet in the initializer, but this method needs to be called if
    * multiple instances of the {@link TextAnnotationMapDBHandler} are used.
    */
  def close() {
    db.commit()
    db.close()
  }

  def useCaching(str: String): Unit = {
    db.close()
    db = DBMaker.fileDB(str).closeOnJvmShutdown().transactionEnable().make()
  }

}
