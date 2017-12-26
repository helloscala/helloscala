package helloscala.cache

import scala.concurrent.{ ExecutionContext, Future }

trait CacheSupport {
  type CacheKey
  type CacheValue

  def saveCache(key: CacheKey, value: CacheValue): Unit

  def getCache(key: CacheKey): Option[CacheValue]

  def findCacheDirect(
    key: CacheKey,
    func: () => Future[CacheValue])(implicit ec: ExecutionContext): Future[CacheValue]

  def findCache(
    key: CacheKey,
    func: () => Future[Option[CacheValue]])(implicit ec: ExecutionContext): Future[Option[CacheValue]]
}
