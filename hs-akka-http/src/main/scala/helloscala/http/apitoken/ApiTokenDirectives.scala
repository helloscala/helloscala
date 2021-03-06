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

package helloscala.http.apitoken

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import helloscala.common.auth.ApiTokenInput
import helloscala.common.types.ObjectId
import helloscala.http.HttpConstants
import helloscala.http.core.server.ApiTokenRejection
import helloscala.common.util.Implicits._

import scala.reflect.ClassTag
import scala.util.control.NonFatal

trait ApiTokenOff[T] {
  val storage: TokenStorage[T]
}

case class DefaultApiTokenOff[T](storage: TokenStorage[T]) extends ApiTokenOff[T]

trait ApiTokenExtractDirectives {

  def extractApiTokenValue[T: ClassTag]: Directive1[T] =
    extractRequestContext
      .flatMap {
        case value: ApiTokenRequestContext[_] if value.tokenValue.isInstanceOf[T] =>
          try {
            provide(value.asInstanceOf[T])
          } catch {
            case NonFatal(e) =>
              reject(ApiTokenRejection("", Some(e)))
          }
        case _ => reject(ApiTokenRejection("需要使用开者者账号凭据进行Token校验"))
      }

}

/**
 * Api Token 指令，用于基于 RESTful API 的身份授权校验
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-30.
 */
trait ApiTokenDirectives extends ApiTokenExtractDirectives {

  def apiTokenOff[T](implicit tokenStorage: TokenStorage[T]): ApiTokenOff[T] =
    DefaultApiTokenOff(tokenStorage)

  /**
   * 获取 [[ApiTokenInput]] 对象
   *
   * @return
   */
  def extractApiTokenInput[T](tokenOff: ApiTokenOff[T]): Directive1[ApiTokenInput] =
    getHeader
      .flatMap { in =>
        onSuccess(tokenOff.storage.lookup(in))
          .flatMap {
            case Right(result) => provide(result)
            case Left(err) =>
              reject(ApiTokenRejection(err.getMessage, Option(err)))
          }
      }

  /**
   * 根据有效的 [[ApiTokenInput]] 对像获取　ApiTokenResult，ApiTokenResult 返回值由 [[TokenStorage.authorization]] 提供
   *
   * @return
   */
  def apiToken[T](tokenOff: ApiTokenOff[T]): Directive1[T] =
    getHeader
      .flatMap { in =>
        onSuccess(tokenOff.storage.authorization(in))
          .flatMap {
            case Right(result) => provide(result)
            case Left(rjt)     => reject(rjt)
          }
      }

  def apiTokenCtx[T](tokenOff: ApiTokenOff[T]): Directive0 =
    getHeader
      .flatMap { in =>
        onSuccess(tokenOff.storage.authorization(in))
          .flatMap {
            case Right(result) =>
              mapRequestContext(rc => new ApiTokenRequestContext(result, rc))
            case Left(rjt) => reject(rjt)
          }
      }

  private def getHeader: Directive1[ApiTokenInput] = {
    def findHeader(ctx: RequestContext, headerName: String) = {
      ctx.request.headers
        .find(_.is(headerName))
        .map(_.value())
        .toEither(headerName)
    }

    extract { ctx =>
      val appId = findHeader(ctx, HttpConstants.HS_APP_ID)
      val timestamp = findHeader(ctx, HttpConstants.HS_TIMESTAMP)
      val echoStr = findHeader(ctx, HttpConstants.HS_ECHO_STR)
      val accessToken = findHeader(ctx, HttpConstants.HS_ACCESS_TOKEN)

      if (appId.isLeft || timestamp.isLeft || echoStr.isLeft || accessToken.isLeft) {
        val msg = Iterator(appId.left.toOption,
                           timestamp.left.toOption,
                           echoStr.left.toOption,
                           accessToken.left.toOption).flatten
          .mkString("需要：", ", ", "")
        Left(msg)
      } else {
        Right(ApiTokenInput(appId.right.get, timestamp.right.get, echoStr.right.get, accessToken.right.get))
      }
    }.flatMap {
      case Left(msg)    => reject(MissingHeaderRejection(msg))
      case Right(value) => provide(value)
    }
  }

  //  private def getCookie: Directive1[String] = cookie("hs-app-id").map(_.value)

}

object ApiTokenDirectives extends ApiTokenDirectives
