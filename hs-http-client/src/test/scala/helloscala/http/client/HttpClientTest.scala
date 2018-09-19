package helloscala.http.client

import javax.net.ssl.{HostnameVerifier, SSLSession}

import helloscala.test.{AkkaSpec, HelloscalaSpec}
import okhttp3.{OkHttpClient, Request}

class HttpClientTest extends HelloscalaSpec with AkkaSpec {

  "HttpClient test" should {
    "1" in {
      //      val url = "http://data.stats.gov.cn/easyquery.htm?m=QueryData&dbcode=csnd&rowcode=zb&colcode=reg&wds=%5B%7B"wdcode":"sj","valuecode":"2007"%7D%5D&dfwds=%5B%7B"wdcode":"zb","valuecode":"A0902"%7D%5D"
      //      val url = """http://data.stats.gov.cn/easyquery.htm?m=QueryData&dbcode=csnd&rowcode=zb&colcode=reg&wds=[{"wdcode":"sj","valuecode":"2016"}]&dfwds=[{"wdcode":"zb","valuecode":"A0401"}]"""
      //      val url = "http://data.stats.gov.cn/easyquery.htm?m=QueryData&dbcode=csnd&rowcode=zb&colcode=reg&wds=%5B%7B%22wdcode%22:%22sj%22,%22valuecode%22:%222007%22%7D%5D&dfwds=%5B%7B%22wdcode%22:%22zb%22,%22valuecode%22:%22A0902%22%7D%5D"
      //      val response = HttpUtils.singleRequest(
      //        HttpMethods.GET,
      //        url,
      //        headers = List(headers.RawHeader("User-Agent", "Mozilla/5.0 (X11; Fedora; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36"))
      //      ).futureValue
      //      //      println(s"url: $realUrl")
      //      HttpUtils.dump(response)

      val url = "https://edoc.xycq.hualongdata.com/api/setting/list"
      val client = new OkHttpClient()
        .newBuilder()
        .hostnameVerifier(new HostnameVerifier() {
          override def verify(hostname: String, session: SSLSession): Boolean = { //强行返回true 即验证成功
            true
          }
        })
        .build()
      val request = new Request.Builder().url(url).build()
      val response = client.newCall(request).execute()
      val str = response.body().string()
      println(str)
    }
  }

}
