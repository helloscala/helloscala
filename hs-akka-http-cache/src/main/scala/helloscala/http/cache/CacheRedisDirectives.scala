package helloscala.http.cache

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ HttpResponse, ResponseEntity }
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import com.redis.RedisClientPool
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.jackson.Jackson
import helloscala.common.util.StringUtils
import helloscala.http.jackson.JacksonSupport
import helloscala.util.SecurityUtils

import scala.concurrent.duration._

trait CacheRedisDirectives {
  def cacheRedisOnParameters(
    redisClientPool: RedisClientPool,
    expiry: FiniteDuration,
    isEncodeCacheKey: Boolean = true): Directive0 =
    CacheRedisDirectives.cacheRedisOnParameters(redisClientPool, expiry)
}

object CacheRedisDirectives extends StrictLogging {

  /**
   * 基于 Redis 的Akka HTTP缓存实现，数据将被序列化成JSON字符串存储到Redis。
   *
   * 1. 只有在 Request Parameters 存在时Cache才会生效。
   * 2. cacheKey生成规则：uri.path + ':' + parameterNames.sorted.map(name => getParameterValue(name)).mkString
   * 3. cacheRedisOnParameters需要放置在正确的位置。需要放置在路由配置的内部，若放置在处部会造成太多无相关的路由被匹配上。
   * 正确方式：
   * pathGet("statistics") {
   *   parameter('day.?) { _day =>
   *     cacheRedisOnParameters(cacheComponent.clients, 61.minutes) {
   *       ....
   *     }
   *   }
   * }
   * 错误方式举例：
   * cacheRedisOnParameters(cacheComponent.clients, 61.minutes) {
   *   pathGet("statistics") {
   *     parameter('day.?) { _day =>
   *       ....
   *     }
   *   }
   * }
   *
   * @param redisClientPool Redis连接池
   * @param expiry 设置缓存超时时间
   * @param isEncodeCacheKey 是否编码 CacheKey，默认true
   * @return
   */
  def cacheRedisOnParameters(
    redisClientPool: RedisClientPool,
    expiry: FiniteDuration,
    isEncodeCacheKey: Boolean = true): Directive0 = {
    extract { ctx =>
      val query = ctx.request.uri.query()
      val keys = query.map(_._1).sorted
      val tmpKey = ctx.request.uri.path.toString() + ':' + keys.flatMap(key => query.get(key)).mkString
      val cacheKey = if (isEncodeCacheKey) {
        val k = SecurityUtils.sha1Hex(tmpKey)
        logger.trace(s"uri: ${ctx.request.uri}, originCacheKey: $tmpKey, encodeCacheKey: $k")
        k
      } else {
        logger.trace("uri: " + ctx.request.uri + ", cacheKey: " + tmpKey)
        tmpKey
      }

      if (StringUtils.isNoneBlank(cacheKey)) {
        val maybe = redisClientPool.withClient(client => client.get[String](cacheKey))
        Some(cacheKey) -> maybe
      } else {
        None -> None
      }
    }.flatMap {
      case (Some(cacheKey), Some(value)) => // 从缓存中获取值并返回
        extractRequestContext
          .flatMap { ctx =>
            logger.debug(s"(${ctx.request.uri})命中缓存Key：$cacheKey, 值：$value")
            val future = Marshal(Jackson.defaultObjectMapper.readTree(value)).to[ResponseEntity](JacksonSupport.marshaller, ctx.executionContext)
            // TODO 若Cache中没找到数据怎么办？
            onSuccess(future)
              .flatMap { v =>
                complete(HttpResponse(entity = v))
              }
          }
      case (Some(cachedKey), _) => // 将响应值设置到缓存
        extractRequestContext
          .flatMap { ctx =>
            import ctx.{ executionContext, materializer }
            mapResponse { response =>
              response.entity.toStrict(10.seconds).onComplete {
                case scala.util.Success(responseEntity) =>
                  val value = responseEntity.data.decodeString("UTF-8")
                  logger.debug(s"(${ctx.request.uri})设置缓存Key：$cachedKey, expiry: $expiry, value：$value")
                  redisClientPool.withClient(client => client.setex(cachedKey, expiry.toMillis, value))

                case scala.util.Failure(e) =>
                  logger.debug(s"(${ctx.request.uri})缓存未命中Key：$cachedKey", e)
              }
              response
            }
          }
      case _ =>
        logger.debug("_, _")
        pass
    }
  }

}
