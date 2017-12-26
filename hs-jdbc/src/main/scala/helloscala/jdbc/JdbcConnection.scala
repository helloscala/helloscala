package helloscala.jdbc

import java.sql.Connection

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-02.
 */

case class JdbcConnection(underlying: Connection, fetchSize: Int = 100)

object JdbcConnection {
  final val empty: JdbcConnection = null
}
