package helloscala.http.client

import akka.http.scaladsl.marshalling.{Marshal, Marshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.{ActorMaterializer, Materializer}
import helloscala.http.HttpUtils

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

final class RichHttpRequest(val request: HttpRequest) extends AnyVal {
  def isEmpty: Boolean = request == null

  def withParams(param: (String, String), params: (String, String)*): RichHttpRequest = {
    val query = request.uri.query().newBuilder.+=(param).++=(params).result()
    withUri(request.uri.withQuery(query))
  }

  def withUri(uri: Uri): RichHttpRequest = new RichHttpRequest(request.copy(uri = uri))

  def withHttpMethod(method: HttpMethod): RichHttpRequest = new RichHttpRequest(request.copy(method = method))

  def withHeaders(hh: Seq[HttpHeader]): RichHttpRequest =
    new RichHttpRequest(request.copy(headers = request.headers ++ hh))

  def withEntity(entity: RequestEntity): RichHttpRequest = new RichHttpRequest(request.copy(entity = entity))

  def withBody[A](body: A)(implicit m: Marshaller[A, RequestEntity], ec: ExecutionContext, atMost: Duration = 10.seconds): RichHttpRequest = {
    val future = Marshal(body).to[RequestEntity]
    val entity = Await.result(future, atMost)
    withEntity(entity)
  }

  def execute(followRedirect: Boolean = true)(implicit mat: ActorMaterializer, ec: ExecutionContext = null): Future[(String, HttpResponse)] = {
    val executionContext: ExecutionContext = if (ec == null) mat.executionContext else ec
    HttpUtils
      .singleRequest(request)
      .flatMap {
        case response if response.status.isRedirection() && followRedirect =>
          val redirectUri = response.headers.find(_.name() == Location.name).get.value()
          HttpUtils.singleRequest(HttpMethods.GET, redirectUri).map(resp => redirectUri -> resp)(executionContext)
        case response =>
          Future.successful(request.uri.toString() -> response)
      }
  }

}

final class RichHttpResponse(val response: HttpResponse) extends AnyVal {
  def isEmpty: Boolean = response == null

  def to[B](implicit um: Unmarshaller[ResponseEntity, B], ec: ExecutionContext = null, mat: Materializer): Future[B] = {
    Unmarshal(response.entity).to[B]
  }

}
