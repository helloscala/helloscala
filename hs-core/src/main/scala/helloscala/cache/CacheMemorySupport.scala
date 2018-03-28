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

import com.google.common.cache.{Cache, CacheBuilder}
import helloscala.common.exception.HSNotFoundException

import scala.concurrent.{ExecutionContext, Future}

trait CacheMemorySupport extends CacheSupport {
  protected val enableCache: Boolean = true
  protected val MAXIMUM_SIZE: Int = 1024
  protected val cache: Cache[CacheKey, CacheValue] = CacheBuilder.newBuilder().maximumSize(MAXIMUM_SIZE).build().asInstanceOf[Cache[CacheKey, CacheValue]]

  override def saveCache(key: CacheKey, value: CacheValue): Unit = {
    if (enableCache) {
      cache.put(key, value)
    }
  }

  override def getCache(key: CacheKey): Option[CacheValue] = if (enableCache) Option(cache.getIfPresent(key)) else None

  override def findCacheDirect(
      key: CacheKey,
      func: () => Future[CacheValue])(implicit ec: ExecutionContext): Future[CacheValue] = {
    findCache(key, () => func().map(v => Some(v))).flatMap {
      case Some(value) => Future.successful(value)
      case None        => Future.failed(HSNotFoundException(s"缓存资源未找到，key: $key"))
    }
  }

  override def findCache(
      key: CacheKey,
      func: () => Future[Option[CacheValue]])(implicit ec: ExecutionContext): Future[Option[CacheValue]] = {
    if (enableCache)
      getCache(key) match {
        case v @ Some(_) => Future.successful(v)
        case _ =>
          func().map { maybe =>
            maybe.foreach(value => saveCache(key, value))
            maybe
          }
      }
    else
      func()
  }

}

object CacheMemorySupport {

}
