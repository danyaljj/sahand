# Sahand 
Because using similarity metrics are often headache. Why? 
 - You have to download many random big files from here and there, and I hate it. 
 - It takes much memory. My experimentation-machine often doesn't have too much memory. And I don't like to use my memory on sth that I don't actively use.  

So I created this for my personal usage. How do I get around these issues? 

 - The software downloads the required resources automatically. You don't have to download anything. 
 - The resources are loaded only on the server side. Client sends calls to the server (and possibly caches them locally). 

## Structure and usage 
The steps are pretty simple. You have to get two machines: 
 - **Server:** this is where the resources (embeddings and all their shit) will be downloaded. So ideally this machine should have a big memory. 
 - **Client:** this is the *light-weight* software that sends calls to the server. 
 
 
 To run the server: 
 
```bash
 > sbt 
 > project server 
 > run
```

With this you should be able to check the api in your browser. Here is an example input: 
`http://localhost:9001/similarity?str1=walking&str2=running&metrics=phrasesim`
 
You can exploit this proframmatically by adding the *client* to your program: 
```sbt
resolvers += "CogcompSoftware" at "http://cogcomp.cs.illinois.edu/m2repo/"
libraryDependencies += "github.sahand" % "sahand-client_2.11" % "1.0"
```

This snippet shows how you can send calls to the server. 
 
```scala 
import github.sahand.SimilarityNames

object ExampleUsage {
  def main(args: Array[String]): Unit = {
    // create a client and send calls
    val client = new SahandClient("http://localhost:9001/")
    println(client.getScore("start", "begin", SimilarityNames.word2vec))
  }
}
```

**Note:** You can write your own client in your favorite language and send calls to the server.   
**Note:** There is MapDB caching on the client side, i.e. each query is saved on disk locally.    


## Supported metrics 
 To see what is supported, import `SimilarityNames` object. Here is a list: 
 
 | Metric    | Explanation                           | Paper?                                                              |
 |-----------|---------------------------------------|---------------------------------------------------------------------|
 | word2vec  |                                       | [Mikolov et al, 2013](https://arxiv.org/abs/1301.3781)              | 
 | phrasesim | Paragraph Vectors (Paragram)          | [Wieting et al, 2015](https://arxiv.org/abs/1507.07998)             | 
 | wnsim     | WNSim WordNet-based Similarity Metric | [Do et al, 2009](http://cogcomp.cs.illinois.edu/papers/DRSTV09.pdf) | 
 | nesim     | Named Entity Similarity Metric        | [Do et al, 2009](http://cogcomp.cs.illinois.edu/papers/DRSTV09.pdf) | 

 
## Future work 
Add support for: 
 - GloVe 
 - ? (you suggest)
 
 
## About the name
*Sahand* (Kurdish/Persian : سهند), is a mountain in East Azerbaijan Province, northwestern Iran. 
It is the highest mountain in the province of East Azarbaijan. ([Read more](https://en.wikipedia.org/wiki/Sahand))
