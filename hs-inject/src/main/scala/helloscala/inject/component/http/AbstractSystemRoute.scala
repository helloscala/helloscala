package helloscala.inject.component.http

import akka.http.scaladsl.server.Route
import helloscala.http.server.AbstractRoute

trait AbstractSystemRoute {
  this: AbstractRoute =>

  val commonRoutes: CommonRoutes

  protected def systemRoute: Route = pathPrefix("_system") {
    commonRoutes.shutdownRoute ~
      commonRoutes.healthCheckRoute
  }

}
