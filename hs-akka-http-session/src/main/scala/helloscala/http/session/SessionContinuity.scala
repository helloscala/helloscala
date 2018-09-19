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

package helloscala.http.session

import scala.concurrent.ExecutionContext

sealed trait SessionContinuity[T] {
  def manager: SessionManager[T]
  def clientSessionManager = manager.clientSessionManager
}

class OneOff[T] private[session] (implicit val manager: SessionManager[T]) extends SessionContinuity[T]

class Refreshable[T] private[session] (
    implicit
    val manager: SessionManager[T],
    val refreshTokenStorage: RefreshTokenStorage[T],
    val ec: ExecutionContext)
    extends SessionContinuity[T] {

  val refreshTokenManager =
    manager.createRefreshTokenManager(refreshTokenStorage)
}
