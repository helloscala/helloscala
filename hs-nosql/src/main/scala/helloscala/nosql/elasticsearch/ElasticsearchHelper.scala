package helloscala.nosql.elasticsearch

import com.sksamuel.elastic4s.xpack.security.XPackElasticClient
import com.sksamuel.elastic4s.{ ElasticsearchClientUri, TcpClient }
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
          XPackElasticClient(
            Settings.builder().put("xpack.security.user", s"$username:$password").build(),
            clientUri)
      })
      .getOrElse(TcpClient.transport(clientUri))
  }

}

object ElasticsearchHelper extends ElasticsearchHelper
