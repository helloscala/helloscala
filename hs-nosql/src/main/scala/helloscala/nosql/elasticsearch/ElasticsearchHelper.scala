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

package helloscala.nosql.elasticsearch

import com.sksamuel.elastic4s.xpack.security.XPackElasticClient
import com.sksamuel.elastic4s.{ElasticsearchClientUri, TcpClient}
import helloscala.common.Configuration
import org.elasticsearch.common.settings.Settings

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-05-23.
 */
class ElasticsearchHelper {

  def tcpClient(config: Configuration): TcpClient = {
    tcpClient(config.get[String]("uri"), config.get[Option[String]]("username"), config.get[Option[String]]("password"))
  }

  def tcpClient(uri: String, maybeUsername: Option[String], maybePassword: Option[String]): TcpClient = {
    // XXX 解决Elasticsearch v5.4.x Netty自动配置处理器核数bug
    System.setProperty("es.set.netty.runtime.available.processors", "false")

    val clientUri = ElasticsearchClientUri(uri)
    val userPassTuple = for {
      username <- maybeUsername
      password <- maybePassword
    } yield username -> password
    userPassTuple
      .map({
        case (username, password) =>
          XPackElasticClient(Settings
                               .builder()
                               .put("xpack.security.user", s"$username:$password")
                               .build(),
                             clientUri)
      })
      .getOrElse(TcpClient.transport(clientUri))
  }

}

object ElasticsearchHelper extends ElasticsearchHelper
