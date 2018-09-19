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

package helloscala.starter.nosql

import javax.inject.{Inject, Provider, Singleton}

import com.google.inject.AbstractModule
import helloscala.common.{AppLifecycle, Configuration, HSCommons}
import helloscala.nosql.cassandra.CassandraSession
import helloscala.nosql.elasticsearch.ESClient
import helloscala.common.util.InternalConfig

@Singleton
class CassandraSessionProvider @Inject()(val config: Configuration, appLifecycle: AppLifecycle)
    extends Provider[CassandraSession] {
  private[this] val session = new DefaultCassandraSession(config, appLifecycle)
  override def get(): CassandraSession = session
}

@Singleton
class ElasticsearchClientProvider @Inject()(configuration: Configuration, appLifecycle: AppLifecycle)
    extends Provider[ESClient] {
  private[this] val client = new DefaultESClient(configuration, appLifecycle)
  override def get() = client
}

class HSNoSQLModule extends AbstractModule {
  private[this] def config = InternalConfig.config

  override def configure(): Unit = {

    if (config.hasPath(HSCommons.CASSANDRA_PATH) && isEnable(HSCommons.CASSANDRA_PATH + ".enable")) {
      bind(classOf[CassandraSession])
        .toProvider(classOf[CassandraSessionProvider])
    }

    if (config.hasPath(HSCommons.ELASTICSEARCH_PATH) && isEnable(HSCommons.ELASTICSEARCH_PATH + ".enable")) {
      bind(classOf[ESClient]).toProvider(classOf[ElasticsearchClientProvider])
    }

  }

  private def isEnable(path: String): Boolean = {
    if (config.hasPath(path)) config.getBoolean(path) else true
  }

}
