package helloscala.slick

import javax.sql.DataSource

import helloscala.common.{ AppLifecycle, Configuration }

import scala.concurrent.Future

trait SchemasTrait {
  val profile: PgProfile.type = PgProfile

  import profile.api._

  val dataSource: DataSource
  val appLifecycle: AppLifecycle
  val configuration: Configuration

  protected val confMaxConnectionsPath = "helloscala.persistence.datasource.maxConnections"
  protected val confKeepAliveConnectionPath = "helloscala.persistence.datasource.keepAliveConnection"
  protected val confNumThreadsPath = "helloscala.persistence.datasource.numThreads"
  protected val confQueueSizePath = "helloscala.persistence.datasource.queueSize"

  val db = Database.forDataSource(
    dataSource,
    configuration.get[Option[Int]](confMaxConnectionsPath),
    executor = AsyncExecutor(
      "helloscala",
      configuration.get[Option[Int]](confNumThreadsPath).getOrElse(20),
      configuration.get[Option[Int]](confQueueSizePath).getOrElse(1000)),
    keepAliveConnection = configuration.get[Option[Boolean]](confKeepAliveConnectionPath).getOrElse(false))
  appLifecycle.addStopHook(() => Future.successful(db.close()))
}
