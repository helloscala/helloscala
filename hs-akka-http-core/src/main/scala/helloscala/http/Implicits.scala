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

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import helloscala.common.exception.HSException
import helloscala.common.jackson.Jackson
import helloscala.common.util.StringUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object Implicits {

  implicit class FutureToObject(future: Future[HttpResponse]) {

    def to[T](implicit ev: ClassTag[T], mat: Materializer, ex: ExecutionContext): Future[Either[HSException, T]] = {
      future
        .flatMap(
          response =>
            Unmarshal(response.entity)
              .to[String]
              .map(str => response.status -> str))
        .map {
          case (status, str) if okStatus(status.intValue()) =>
            if (StringUtils.isNoneBlank(str))
              Right(
                Jackson.defaultObjectMapper
                  .readValue(str, ev.runtimeClass)
                  .asInstanceOf[T])
            else
              Right(str.asInstanceOf[T])
          case (status, str) =>
            Left(
              Jackson.defaultObjectMapper
                .readValue(str, classOf[HSException])
                .asInstanceOf[HSException])
        }
    }
  }

  private def okStatus(id: Int) = id >= 200 && id < 299

}
