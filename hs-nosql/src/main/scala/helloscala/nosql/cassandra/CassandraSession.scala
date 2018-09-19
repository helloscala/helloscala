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

package helloscala.nosql.cassandra

import java.util

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.{Sink, Source}
import com.datastax.driver.core._
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture, MoreExecutors}
import com.typesafe.scalalogging.StrictLogging

import scala.annotation.varargs
import scala.collection.immutable
import scala.concurrent.{Future, Promise}

private class CqlCache(session: => Session) extends StrictLogging {
  private val map = new util.HashMap[String, PreparedStatement]()

  def putIfAbsent(cql: String): PreparedStatement = synchronized {
    if (map.containsKey(cql)) {
      map.get(cql)
    } else {
      logger.debug("new cql: {}", cql)
      val pstmt = session.prepare(cql)
      map.put(cql, pstmt)
      pstmt
    }
  }

}

private[cassandra] object GuavaFutures {

  implicit final class GuavaFutureOpts[A](val guavaFut: ListenableFuture[A]) extends AnyVal {

    def asScala(): Future[A] = {
      val p = Promise[A]()
      val callback = new FutureCallback[A] {
        override def onSuccess(a: A): Unit = p.success(a)

        override def onFailure(err: Throwable): Unit = p.failure(err)
      }
      Futures.addCallback(guavaFut, callback, MoreExecutors.directExecutor())
      p.future
    }
  }

}

trait CassandraSession {

  import GuavaFutures._

  val conf: CassandraConf

  val cluster: Cluster

  /**
   * 获得 Cassandra 连接 Session
   */
  implicit protected lazy val defaultSession: Session = conf.keyspace match {
    case None           => cluster.connect()
    case Some(keyspace) => cluster.connect(keyspace)
  }

  private lazy val cqlCache = new CqlCache(defaultSession)

  @varargs
  def registerCodec(codes: TypeCodec[_]*): Unit = {
    cluster.getConfiguration.getCodecRegistry.register(codes: _*)
  }

  /**
   * 生成预编译 CQL 语句
   *
   * @param cql Cassandra CQL 语句
   * @return 若存在就直接返回，不存在则生成返回并缓存
   */
  def prepare(cql: String): PreparedStatement = cqlCache.putIfAbsent(cql)

  def source(stmt: Statement): Source[Row, NotUsed] = CassandraSource(stmt)

  def runHead(stmt: Statement)(implicit mat: Materializer): Future[Row] =
    CassandraSource(stmt).runWith(Sink.head)

  def runSeq(stmt: Statement)(implicit mat: Materializer): Future[immutable.Seq[Row]] =
    CassandraSource(stmt).runWith(Sink.seq)

  def executeAsync(stmt: Statement): Future[ResultSet] =
    defaultSession.executeAsync(stmt).asScala()

  def executeAsync(stmt: String): Future[ResultSet] =
    defaultSession.executeAsync(stmt).asScala()

  def execute(stmt: Statement): ResultSet = defaultSession.execute(stmt)

  def execute(stmt: String): ResultSet = defaultSession.execute(stmt)

  def close(): Unit = {
    if (cluster != null) {
      cluster.close()
    }
  }

}

//abstract class StandaloneCassandraSession(val conf: CassandraConf, val cluster: Cluster) extends CassandraSession
