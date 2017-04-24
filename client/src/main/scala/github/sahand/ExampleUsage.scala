package github.sahand

import sahand.SimilarityType

object ExampleUsage {
  def main(args: Array[String]): Unit = {
    // create a client and send calls
    val client = new SahandClient("http://localhost:9001/")
    println(client.getScore("start", "begin", SimilarityType.word2vec))
  }
}
