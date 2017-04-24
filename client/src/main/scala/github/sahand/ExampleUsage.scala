package github.sahand

object ExampleUsage {
  def main(args: Array[String]): Unit = {
    // create a client and send calls
    val client = new SahandClient("http://localhost:9001/")
    println(client.getScore("start", "begin", SimilarityNames.word2vec))
  }
}
