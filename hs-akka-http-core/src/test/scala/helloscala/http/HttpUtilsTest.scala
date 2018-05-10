package helloscala.http

import akka.http.scaladsl.model.Uri
import helloscala.test.HelloscalaSpec

class HttpUtilsTest extends HelloscalaSpec {

  "HttpUtils" should {
    "Uri.withQuery" in {
      val uri = Uri("http://localhost:8080/validate?token=3322")
      println(uri)
      val uri2 = uri.withQuery(Uri.Query(List("name" -> "yangbajing"): _*))
      println(uri2)
    }
  }

}
