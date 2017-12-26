package helloscala.http.session

import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ HttpRequest, StatusCodes, Uri }
import akka.http.scaladsl.server.{ Rejection, RequestContext, RouteResult }
import akka.http.scaladsl.settings.{ ParserSettings, RoutingSettings }
import akka.stream.Materializer

import scala.concurrent.{ ExecutionContextExecutor, Future }

case class SessionRequestContext[T](session: T, rc: RequestContext) extends RequestContext {
  println("init SessionRequestContext")

  override val request: HttpRequest = rc.request
  override val unmatchedPath: Uri.Path = rc.unmatchedPath

  override implicit def executionContext: ExecutionContextExecutor = rc.executionContext

  override implicit def materializer: Materializer = rc.materializer

  override def log: LoggingAdapter = rc.log

  override def settings: RoutingSettings = rc.settings

  override def parserSettings: ParserSettings = rc.parserSettings

  override def reconfigure(executionContext: ExecutionContextExecutor, materializer: Materializer, log: LoggingAdapter, settings: RoutingSettings): RequestContext =
    rc.reconfigure(executionContext, materializer, log, settings)

  override def complete(obj: ToResponseMarshallable): Future[RouteResult] = rc.complete(obj)

  override def reject(rejections: Rejection*): Future[RouteResult] = rc.reject(rejections: _*)

  override def redirect(uri: Uri, redirectionType: StatusCodes.Redirection): Future[RouteResult] = rc.redirect(uri, redirectionType)

  override def fail(error: Throwable): Future[RouteResult] = rc.fail(error)

  override def withRequest(req: HttpRequest): RequestContext = rc.withRequest(req)

  override def withExecutionContext(ec: ExecutionContextExecutor): RequestContext = rc.withExecutionContext(ec)

  override def withMaterializer(materializer: Materializer): RequestContext = rc.withMaterializer(materializer)

  override def withLog(log: LoggingAdapter): RequestContext = rc.withLog(log)

  override def withRoutingSettings(settings: RoutingSettings): RequestContext = rc.withRoutingSettings(settings)

  override def withParserSettings(settings: ParserSettings): RequestContext = rc.withParserSettings(settings)

  override def mapRequest(f: HttpRequest => HttpRequest): RequestContext = rc.mapRequest(f)

  override def withUnmatchedPath(path: Uri.Path): RequestContext = rc.withUnmatchedPath(path)

  override def mapUnmatchedPath(f: Uri.Path => Uri.Path): RequestContext = rc.mapUnmatchedPath(f)

  override def withAcceptAll: RequestContext = rc.withAcceptAll

  override def toString = super.toString
}
