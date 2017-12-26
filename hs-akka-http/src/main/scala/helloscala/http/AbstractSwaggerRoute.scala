package helloscala.http

import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import helloscala.common.Configuration
import helloscala.http.server.AbstractRoute

trait AbstractSwaggerRoute {
  this: AbstractRoute =>

  val configuration: Configuration

  val swagger = SwaggerHttpDoc(configuration)

  protected def swaggerRoute: Route = cors()(
    swagger.routes ~
      swagger.swaggerUiRoute)

}
