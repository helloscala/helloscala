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

package helloscala.http

import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import helloscala.common.Configuration
import helloscala.http.server.AbstractRoute

trait AbstractSwaggerRoute {
  this: AbstractRoute =>

  val configuration: Configuration

  val swagger = SwaggerHttpDoc(configuration)

  protected def swaggerRoute: Route =
    cors()(
      swagger.routes ~
        swagger.swaggerUiRoute)

}
