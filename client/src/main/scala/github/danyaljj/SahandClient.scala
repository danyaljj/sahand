package github.danyaljj

import java.net.URLEncoder

import scala.io.Source

/**
  * This client talks with the Sahand server
  * @param endPoint the url of the Sahand server. Example is: "http://localhost:9001/"
  */
class SahandClient(endPoint: String) {

  def getScore(text1: String, text2: String, metric: String): Double = {
    val json = get(creatURL(text1, text2, metric))
    import play.api.libs.json.Json
    import play.api.libs.json._
    val parsedJson = Json.parse(json)
    val headResult = parsedJson.as[JsArray].value.head
    (headResult \ "score").validate[Double].get
  }

  private def creatURL(text1: String, text2: String, metrics: String): String = {
    endPoint + "similarity?str1="+ URLEncoder.encode(text1, "UTF-8") + "&str2=" + URLEncoder.encode(text2, "UTF-8") +
      "&metrics=" + URLEncoder.encode(metrics, "UTF-8")
  }

  @throws(classOf[java.io.IOException])
  private def get(url: String) = Source.fromURL(url).mkString
}
