package helloscala.inject

import javax.inject.{Inject, Provider, Singleton}
import javax.sql.DataSource

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.AbstractModule
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import com.zaxxer.hikari.HikariDataSource
import helloscala.common.{AppLifecycle, Configuration}
import helloscala.inject.component.DefaultAppLifecycle
import helloscala.jdbc.{JdbcOperations, JdbcTemplate, JdbcUtils}
import helloscala.common.util.InternalConfig

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

//@Singleton
//class ConfigProvider extends Provider[Config] {
//  override def get(): Config = {
//    // TODO 考虑实现为从服务发现系统里获取
//    ConfigFactory.load()
//  }
//}

@Singleton
class ConfigurationProvider @Inject() (config: Config) extends Provider[Configuration] {
  private[this] val configuration = Configuration(config)

  override def get(): Configuration = configuration
}

@Singleton
class ActorSystemProvider @Inject() (
    configuration: Configuration,
    appLifecycle: AppLifecycle) extends Provider[ActorSystem] {

  private[this] val system = ActorSystem(
    configuration.get[Option[String]]("helloscala.akka-system-name").getOrElse("hs"),
    configuration.underlying)
  appLifecycle.addStopHook(() => Future.successful(system.terminate()))

  override def get(): ActorSystem = system
}

@Singleton
class ActorMaterializerProvider @Inject() (actorSystem: ActorSystem) extends Provider[ActorMaterializer] {
  private[this] val mat = ActorMaterializer()(actorSystem)

  override def get(): ActorMaterializer = mat
}

@Singleton
class ExecutionContextExecutorProvider @Inject() (actorSystem: ActorSystem) extends Provider[ExecutionContextExecutor] {
  override def get(): ExecutionContextExecutor = actorSystem.dispatcher
}

class HSBuiltinModule(config: Config) extends AbstractModule with StrictLogging {

  def this() {
    this(InternalConfig.config)
  }

  override def configure(): Unit = {
    logger.info(
      "configuration: \n{}\n{}\n{}",
      if (config.hasPath("intelligence")) config.getConfig("intelligence") else ConfigFactory.empty(),
      config.getConfig("helloscala"),
      if (config.hasPath("server")) config.getConfig("server") else ConfigFactory.empty())

    bind(classOf[Config]).toInstance(config) // .toProvider(classOf[ConfigProvider])
    bind(classOf[Configuration]).toProvider(classOf[ConfigurationProvider])

    bind(classOf[ActorSystem]).toProvider(classOf[ActorSystemProvider])

    bind(classOf[AppLifecycle]).to(classOf[DefaultAppLifecycle])
    bind(classOf[ActorMaterializer]).toProvider(classOf[ActorMaterializerProvider])
    bind(classOf[Materializer]).to(classOf[ActorMaterializer])

    bind(classOf[ExecutionContextExecutor]).toProvider(classOf[ExecutionContextExecutorProvider])
    bind(classOf[ExecutionContext]).to(classOf[ExecutionContextExecutor])

    if (config.hasPath(JdbcUtils.DATASOURCE_PATH) && isEnable(JdbcUtils.DATASOURCE_PATH + ".enable")) {
      bind(classOf[HikariDataSource]).toProvider(classOf[DefaultDataSourceProvider])
      bind(classOf[DataSource]).to(classOf[HikariDataSource])
      bind(classOf[JdbcTemplate]).toProvider(classOf[DefaultJdbcTemplateProvider])
      bind(classOf[JdbcOperations]).to(classOf[JdbcTemplate])
    }

    logger.info(s"configure completed.")
  }

  private def isEnable(path: String): Boolean = {
    if (config.hasPath(path)) config.getBoolean(path) else true
  }

}
