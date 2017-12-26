package helloscala.starter.nosql

import com.datastax.driver.core.Cluster
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.{ AppLifecycle, Configuration }
import helloscala.nosql.cassandra.{ CassandraConf, CassandraHelper, CassandraSession }

import scala.concurrent.Future
import scala.util.control.NonFatal

class DefaultCassandraSession(
  configuration: Configuration,
  appLifecycle: AppLifecycle) extends CassandraSession with StrictLogging {

  val conf = CassandraConf(configuration, "helloscala.persistence.cassandra")
  override val cluster: Cluster = CassandraHelper.getCluster(conf)

  appLifecycle.addStopHook(() => Future.successful(try {
    cluster.close()
    logger.info("关闭Cassandra连接完成")
  } catch {
    case NonFatal(e) =>
      logger.error("关闭Cassandra连接错误", e)
  }))

}
