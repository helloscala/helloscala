package helloscala.starter.nosql

import com.sksamuel.elastic4s.TcpClient
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.{ AppLifecycle, Configuration }
import helloscala.nosql.elasticsearch.{ ESClient, ElasticsearchHelper }

import scala.concurrent.Future
import scala.util.control.NonFatal

class DefaultESClient(
  configuration: Configuration,
  appLifecycle: AppLifecycle) extends ESClient with StrictLogging {
  val pathPrefix = "helloscala.persistence.elasticsearch"

  val client: TcpClient = ElasticsearchHelper.tcpClient(configuration.get[Configuration](pathPrefix))

  appLifecycle.addStopHook(() => Future.successful(try {
    client.close()
    logger.info("关闭Elasticsearch连接完成")
  } catch {
    case NonFatal(e) =>
      logger.error(s"停止ElasticsearchClient连接错误，$client", e)
  }))
}
