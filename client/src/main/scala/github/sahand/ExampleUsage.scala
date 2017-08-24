package github.sahand

object ExampleUsage {
  def main(args: Array[String]): Unit = {
    // create a client and send calls
    val client = new SahandClient("http://smeagol.cs.illinois.edu:8080/")
    println(client.getScore("start", "begin", SimilarityNames.word2vec))
  }
}
