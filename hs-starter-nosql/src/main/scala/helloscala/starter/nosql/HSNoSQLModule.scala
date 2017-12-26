package helloscala.starter.nosql

import javax.inject.{ Inject, Provider, Singleton }

import com.google.inject.AbstractModule
import helloscala.common.{ AppLifecycle, Configuration, HSCommons }
import helloscala.nosql.cassandra.CassandraSession
import helloscala.nosql.elasticsearch.ESClient
import helloscala.util.InternalConfig
import org.elasticsearch.client.ElasticsearchClient

@Singleton
class CassandraSessionProvider @Inject() (
  val config: Configuration,
  appLifecycle: AppLifecycle) extends Provider[CassandraSession] {
  private[this] val session = new DefaultCassandraSession(config, appLifecycle)
  override def get(): CassandraSession = session
}

@Singleton
class ElasticsearchClientProvider @Inject() (
  configuration: Configuration,
  appLifecycle: AppLifecycle) extends Provider[ESClient] {
  private[this] val client = new DefaultESClient(configuration, appLifecycle)
  override def get() = client
}

class HSNoSQLModule extends AbstractModule {
  private[this] def config = InternalConfig.config

  def configure(): Unit = {

    if (config.hasPath(HSCommons.CASSANDRA_PATH) && isEnable(HSCommons.CASSANDRA_PATH + ".enable")) {
      bind(classOf[CassandraSession]).toProvider(classOf[CassandraSessionProvider])
    }

    if (config.hasPath(HSCommons.ELASTICSEARCH_PATH) && isEnable(HSCommons.ELASTICSEARCH_PATH + ".enable")) {
      bind(classOf[ESClient]).toProvider(classOf[ElasticsearchClientProvider])
    }

  }

  private def isEnable(path: String): Boolean = {
    if (config.hasPath(path)) config.getBoolean(path) else true
  }

}
