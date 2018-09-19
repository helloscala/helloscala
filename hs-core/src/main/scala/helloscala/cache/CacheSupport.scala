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

package helloscala.cache

import scala.concurrent.{ExecutionContext, Future}

trait CacheSupport {
  type CacheKey
  type CacheValue

  def saveCache(key: CacheKey, value: CacheValue): Unit

  def getCache(key: CacheKey): Option[CacheValue]

  def findCacheDirect(key: CacheKey, func: () => Future[CacheValue])(implicit ec: ExecutionContext): Future[CacheValue]

  def findCache(key: CacheKey, func: () => Future[Option[CacheValue]])(
      implicit ec: ExecutionContext): Future[Option[CacheValue]]
}
