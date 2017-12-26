package helloscala.http.client

import javax.net.ssl.{HostnameVerifier, SSLSession}

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.stream.ActorMaterializer
import helloscala.http.HttpUtils
import okhttp3.OkHttpClient

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

object HttpClient {

  def okHttpClient(disableHostnameVerifier: Boolean): OkHttpClient = {
    val builder = new OkHttpClient().newBuilder()
    if (disableHostnameVerifier) {
      builder.hostnameVerifier(new HostnameVerifier {
        override def verify(s: String, sslSession: SSLSession): Boolean = true
      })
    }
    builder.build()
  }

  def execute(
      method: HttpMethod = HttpMethods.GET,
      uri: Uri = Uri./,
      params: Seq[(String, String)] = Nil,
      httpHeaders: immutable.Seq[HttpHeader] = Nil,
      data: AnyRef = null,
      protocol: HttpProtocol = HttpProtocols.`HTTP/1.1`,
      followRedirect: Boolean = true
  )(implicit mat: ActorMaterializer, ec: ExecutionContext): Future[(String, HttpResponse)] = {
    HttpUtils
      .singleRequest(method, uri, params, data, httpHeaders, protocol)
      .flatMap {
        case response if response.status.isRedirection() && followRedirect =>
          val redirectUri = response.headers.find(_.name() == Location.name).get.value()
          println(s"use followRedirect: $redirectUri")
          HttpUtils.singleRequest(HttpMethods.GET, redirectUri).map(resp => redirectUri -> resp)
        case response => Future.successful(uri.toString() -> response)
      }
  }

}
