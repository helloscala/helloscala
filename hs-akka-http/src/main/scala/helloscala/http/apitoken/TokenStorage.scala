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

import akka.http.scaladsl.server.Rejection
import helloscala.common.auth.ApiTokenInput
import helloscala.common.exception.HSException

import scala.concurrent.Future

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-30.
 */
trait TokenStorage[T] {

  /**
   * 判断 ApiTokenInput 是否有效
   *
   * @param in
   * @return
   */
  def lookup(in: ApiTokenInput): Future[Either[HSException, ApiTokenInput]]

  /**
   * 校验 Api Token 认证会话，并转换成 T 类型
   *
   * @return
   */
  def authorization(apiToken: ApiTokenInput): Future[Either[Rejection, T]]
}
