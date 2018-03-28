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

import com.sksamuel.elastic4s.TcpClient
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.{AppLifecycle, Configuration}
import helloscala.nosql.elasticsearch.{ESClient, ElasticsearchHelper}

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
