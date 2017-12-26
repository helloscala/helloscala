package helloscala.nosql.elasticsearch

import com.sksamuel.elastic4s.TcpClient

trait ESClient {
  val client: TcpClient
}
