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

package helloscala.jdbc

import java.sql.{Connection, PreparedStatement, SQLException}
import javax.sql.DataSource

import scala.collection.mutable
import scala.util.control.NonFatal

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-02-21.
 */
class JdbcHelper(dataSource: DataSource) {

  /**
   * 执行insert, update, delete, alert 语句
   *
   * @param sql    SQL语句
   * @param args   占位符参数
   * @param hlConn JDBC连接
   * @return 返回语句成功执行后影响的数据行数
   * @throws SQLException SQL执行或数据库异常
   */
  @throws(classOf[SQLException])
  def executeUpdate(sql: String, args: Object*)(
      implicit
      hlConn: JdbcConnection = JdbcConnection.empty): Int = {
    val func = (pstmt: PreparedStatement) => pstmt.executeUpdate()

    execute(func, sql, args)
  }

  def executeSingle(sql: String, args: Object*)(
      implicit
      igConn: JdbcConnection = JdbcConnection.empty): Option[(Map[String, Object], Vector[HSSqlMetaData])] = {
    val (results, metaDatas) = executeQuery(sql, args)
    //    if (results.size > 1) throw new IllegalAccessException(s"$sql 返回结果大于1行")
    results.headOption.map(map => (map, metaDatas))
  }

  def executeQuery(sql: String, args: Object*)(
      implicit
      igConn: JdbcConnection = JdbcConnection.empty): (Vector[Map[String, Object]], Vector[HSSqlMetaData]) = {
    val func = (pstmt: PreparedStatement) => {
      val results = mutable.ArrayBuffer.empty[Map[String, Object]]
      val rs = pstmt.executeQuery()
      val metaData = rs.getMetaData

      val range = (1 to metaData.getColumnCount).toVector

      val labels = range.map(column =>
        HSSqlMetaData(metaData.getColumnLabel(column), metaData.getColumnName(column), metaData.getColumnType(column)))

      while (rs.next()) {
        val maps = range
          .map(column => labels(column - 1).label -> rs.getObject(column))
          .toMap
        results += maps
      }
      (results.toVector, labels)
    }

    execute(func, sql, args)
  }

  def execute[R](resultSetFunc: PreparedStatement => R, sql: String, args: Object*)(
      implicit
      igConn: JdbcConnection = JdbcConnection.empty): R =
    _execute { conn =>
      val pstmt = conn.underlying.prepareStatement(sql)
      for ((arg, idx) <- args.zipWithIndex) {
        pstmt.setObject(idx + 1, arg)
      }

      resultSetFunc(pstmt)
    }

  private def _execute[R](func: JdbcConnection => R)(
      implicit
      igConn: JdbcConnection = JdbcConnection.empty): R = {
    val conn =
      if (igConn == JdbcConnection.empty)
        JdbcConnection(dataSource.getConnection)
      else igConn
    try {
      func(conn)
    } finally {
      if (!conn.underlying.isClosed)
        conn.underlying.close()
    }
  }

  def doInTransaction[R](func: JdbcConnection => R): R = {
    val conn = JdbcConnection(dataSource.getConnection)
    val originalCommit = conn.underlying.getAutoCommit
    try {
      conn.underlying.setAutoCommit(false)
      val result = func(conn)
      conn.underlying.commit()
      result
    } catch {
      case NonFatal(e) =>
        conn.underlying.rollback()
        throw e
    } finally {
      conn.underlying.setAutoCommit(originalCommit)
      if (!conn.underlying.isClosed)
        conn.underlying.close()
    }
  }

}

object JdbcHelper {

  def apply(ds: DataSource): JdbcHelper = new JdbcHelper(ds)

  def execute[R](conn: Connection, resultSetFunc: PreparedStatement => R, sql: String, args: Object*): R = {
    execute { conn =>
      val pstmt = conn.prepareStatement(sql)
      for ((arg, idx) <- args.zipWithIndex) {
        pstmt.setObject(idx + 1, arg)
      }

      resultSetFunc(pstmt)
    }(conn)
  }

  def execute[R](func: Connection => R)(conn: Connection): R = {
    try {
      func(conn)
    } finally {
      if (conn != null && !conn.isClosed)
        conn.close()
    }
  }

}

//object SqlTemplate {
//  def main(args: Array[String]): Unit = {
//    val helper = new SqlTemplate(null)
//    import helper._
//
//    doInTransaction { implicit conn =>
//      executeQuery("")
//      executeQuery("")
//      executeQuery("")
//    }
//  }
//}
