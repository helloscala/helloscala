/*
 * Copyright 2017 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helloscala.http.session

import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpRequest, StatusCodes, Uri}
import akka.http.scaladsl.server.{Rejection, RequestContext, RouteResult}
import akka.http.scaladsl.settings.{ParserSettings, RoutingSettings}
import akka.stream.Materializer

import scala.concurrent.{ExecutionContextExecutor, Future}

case class SessionRequestContext[T](session: T, rc: RequestContext) extends RequestContext {
  println("init SessionRequestContext")

  override val request: HttpRequest = rc.request
  override val unmatchedPath: Uri.Path = rc.unmatchedPath

  implicit override def executionContext: ExecutionContextExecutor =
    rc.executionContext

  implicit override def materializer: Materializer = rc.materializer

  override def log: LoggingAdapter = rc.log

  override def settings: RoutingSettings = rc.settings

  override def parserSettings: ParserSettings = rc.parserSettings

  override def reconfigure(
      executionContext: ExecutionContextExecutor,
      materializer: Materializer,
      log: LoggingAdapter,
      settings: RoutingSettings): RequestContext =
    rc.reconfigure(executionContext, materializer, log, settings)

  override def complete(obj: ToResponseMarshallable): Future[RouteResult] =
    rc.complete(obj)

  override def reject(rejections: Rejection*): Future[RouteResult] =
    rc.reject(rejections: _*)

  override def redirect(uri: Uri, redirectionType: StatusCodes.Redirection): Future[RouteResult] =
    rc.redirect(uri, redirectionType)

  override def fail(error: Throwable): Future[RouteResult] = rc.fail(error)

  override def withRequest(req: HttpRequest): RequestContext =
    rc.withRequest(req)

  override def withExecutionContext(ec: ExecutionContextExecutor): RequestContext =
    rc.withExecutionContext(ec)

  override def withMaterializer(materializer: Materializer): RequestContext =
    rc.withMaterializer(materializer)

  override def withLog(log: LoggingAdapter): RequestContext = rc.withLog(log)

  override def withRoutingSettings(settings: RoutingSettings): RequestContext =
    rc.withRoutingSettings(settings)

  override def withParserSettings(settings: ParserSettings): RequestContext =
    rc.withParserSettings(settings)

  override def mapRequest(f: HttpRequest => HttpRequest): RequestContext =
    rc.mapRequest(f)

  override def withUnmatchedPath(path: Uri.Path): RequestContext =
    rc.withUnmatchedPath(path)

  override def mapUnmatchedPath(f: Uri.Path => Uri.Path): RequestContext =
    rc.mapUnmatchedPath(f)

  override def withAcceptAll: RequestContext = rc.withAcceptAll

  override def toString = super.toString
}
