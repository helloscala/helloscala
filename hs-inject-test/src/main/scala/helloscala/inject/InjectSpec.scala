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

package helloscala.inject

import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.SourceQueueWithComplete
import helloscala.common.Configuration
import helloscala.http.HttpUtils
import helloscala.inject.component.DefaultAppLifecycle
import helloscala.test.HelloscalaSpec
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Promise}

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-05-05.
 */
trait InjectSpec extends BeforeAndAfterAll with InjectSystemSupport {
  this: HelloscalaSpec =>

  protected def configuration: Configuration = instance[Configuration]

  implicit protected def executionContext: ExecutionContext =
    instance[ExecutionContext]

  implicit protected def actorMaterializer: ActorMaterializer =
    instance[ActorMaterializer]

  implicit protected lazy val httpSourceQueue: SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] =
    configuration.get[Option[Int]]("server.https-port") match {
      case Some(httpsPort) =>
        HttpUtils.cachedHostConnectionPoolHttps(configuration.getString("server.host"), httpsPort)
      case _ =>
        HttpUtils.cachedHostConnectionPool(configuration.getString("server.host"), configuration.getInt("server.port"))
    }

  override protected def afterAll(): Unit = {
    val appLifecycle = instance[DefaultAppLifecycle]
    val stopResult = Await.result(appLifecycle.stop(), Duration.Inf)
    logger.info("{} Stopped, result: {}", this.getClass.getSimpleName, stopResult)
    super.afterAll()
  }

}
