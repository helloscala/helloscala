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

package helloscala.test

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.jackson.Jackson
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

trait TSpec extends MustMatchers with OptionValues with EitherValues with ScalaFutures {
  this: Suite =>

  val defaultObjectMapper: ObjectMapper = Jackson.defaultObjectMapper

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(Span(90, Seconds), Span(100, Millis))

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

  implicit def executionContext: ExecutionContextExecutor =
    actorSystem.dispatcher

  override protected def afterAll(): Unit = {
    Await.result(actorSystem.terminate(), 60.seconds)
  }

}
