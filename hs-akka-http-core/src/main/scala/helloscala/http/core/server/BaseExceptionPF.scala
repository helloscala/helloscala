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

package helloscala.http.core.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.ExceptionHandler
import akka.pattern.AskTimeoutException
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.data.ApiResult
import helloscala.common.exception.HSException

/**
 * 基本异常处理函数
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-01.
 */
trait BaseExceptionPF extends StrictLogging {

  def exceptionHandlerPF: ExceptionHandler.PF = {
    case ex: Exception =>
      extractUri { uri =>
        val response = ex match {
          case e: HSException =>
            val t = e.getCause
            //            if (t != null) logger.warn(s"URI[$uri] ${e.toString}", t) else logger.warn(s"URI[$uri] ${e.toString}")
            logger.warn(s"Exception[${e.getClass.getSimpleName}] URI[$uri] ${e.getMessage}", t)
            (StatusCodes.getForKey(e.getHttpStatus).getOrElse(StatusCodes.Conflict), ApiResult.error(e.getErrCode, e.getErrMsg, e.getData))

          case e: IllegalArgumentException =>
            logger.debug(s"Illegal Argument: ${e.getMessage}", e)
            (StatusCodes.BadRequest, ApiResult.error(StatusCodes.BadRequest.intValue, e.getLocalizedMessage))

          case e: AskTimeoutException =>
            logger.debug(s"Actor Timeout: ${e.getMessage}", e)
            (StatusCodes.GatewayTimeout, ApiResult.error(StatusCodes.GatewayTimeout.intValue, "请求超时"))

          case _ =>
            logger.error(s"请求无法正常处理，URI[$uri]", ex)
            (StatusCodes.InternalServerError, ApiResult.error(StatusCodes.InternalServerError.intValue, ex.getMessage))
        }
        import helloscala.http.jackson.JacksonSupport._
        complete(response)
      }
  }

  final val exceptionHandler = ExceptionHandler(exceptionHandlerPF)
}
