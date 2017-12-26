package helloscala.test

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.{ ObjectMapper, SerializationFeature }
import com.typesafe.scalalogging.StrictLogging
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Millis, Seconds, Span }

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContextExecutor, Future }

trait TSpec
  extends MustMatchers
  with OptionValues
  with EitherValues
  with ScalaFutures {
  this: Suite =>

  val defaultObjectMapper: ObjectMapper =
    new ObjectMapper()
      .findAndRegisterModules()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

  implicit val defaultPatience = PatienceConfig(Span(90, Seconds), Span(100, Millis))

  def jsonPrettyString[T](f: Future[T]): String = {
    val results = f.futureValue
    val writer = defaultObjectMapper.writer(new DefaultPrettyPrinter())
    writer.writeValueAsString(results)
  }

  def jsonPrettyString(obj: AnyRef): String = {
    val writer = defaultObjectMapper.writer(new DefaultPrettyPrinter())
    writer.writeValueAsString(obj)
  }

  def jsonString[T](f: Future[T]): String = {
    val results = f.futureValue
    defaultObjectMapper.writeValueAsString(results)
  }

  def jsonString(obj: AnyRef): String = {
    defaultObjectMapper.writeValueAsString(obj)
  }

}

trait HelloscalaSpec extends WordSpec with TSpec with StrictLogging

trait AkkaSpec extends BeforeAndAfterAll {
  this: HelloscalaSpec =>

  implicit def actorSystem: ActorSystem = ActorSystem("akka-spec")

  implicit def actorMaterializer: ActorMaterializer = ActorMaterializer()

  implicit def executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  override protected def afterAll(): Unit = {
    Await.result(actorSystem.terminate(), 60.seconds)
  }

}
