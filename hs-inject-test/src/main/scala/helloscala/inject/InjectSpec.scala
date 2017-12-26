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
import scala.concurrent.{ Await, ExecutionContext, Promise }

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-05-05.
 */
trait InjectSpec extends BeforeAndAfterAll with InjectSystemSupport {
  this: HelloscalaSpec =>

  protected def configuration: Configuration = instance[Configuration]

  protected implicit def executionContext: ExecutionContext = instance[ExecutionContext]

  protected implicit def actorMaterializer: ActorMaterializer = instance[ActorMaterializer]

  protected implicit lazy val httpSourceQueue: SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] =
    configuration.get[Option[Int]]("server.https-port") match {
      case Some(httpsPort) => HttpUtils.cachedHostConnectionPoolHttps(configuration.getString("server.host"), httpsPort)
      case _ => HttpUtils.cachedHostConnectionPool(configuration.getString("server.host"), configuration.getInt("server.port"))
    }

  override protected def afterAll(): Unit = {
    val appLifecycle = instance[DefaultAppLifecycle]
    val stopResult = Await.result(appLifecycle.stop(), Duration.Inf)
    logger.info("{} Stopped, result: {}", this.getClass.getSimpleName, stopResult)
    super.afterAll()
  }

}
