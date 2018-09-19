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

import com.datastax.driver.core.Cluster
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.{AppLifecycle, Configuration}
import helloscala.nosql.cassandra.{CassandraConf, CassandraHelper, CassandraSession}

import scala.concurrent.Future
import scala.util.control.NonFatal

class DefaultCassandraSession(configuration: Configuration, appLifecycle: AppLifecycle)
    extends CassandraSession
    with StrictLogging {

  val conf = CassandraConf(configuration, "helloscala.persistence.cassandra")
  override val cluster: Cluster = CassandraHelper.getCluster(conf)

  appLifecycle.addStopHook(() =>
    Future.successful(try {
      cluster.close()
      logger.info("关闭Cassandra连接完成")
    } catch {
      case NonFatal(e) =>
        logger.error("关闭Cassandra连接错误", e)
    }))

}
