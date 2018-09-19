package helloscala.nosql.cassandra

import java.util.UUID

import akka.stream.alpakka.cassandra.scaladsl.CassandraSource
import akka.stream.scaladsl.Sink
import com.datastax.driver.core.{Cluster, Session, SimpleStatement}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-07.
 */
class AlpakkaCassandraTest extends CassandraSpec {
  implicit override val session: Session = Cluster.builder.addContactPoint("127.0.0.1").withPort(9042).build.connect()

  "AlpakkaCassandraTest" must {
    "INSERST" in {
      val preparedStatement = session.prepare("INSERT INTO akka_stream_scala_test.test(id, name) VALUES (?, ?)")
      val boundStmt = preparedStatement.bind(UUID.randomUUID().toString, "羊八井")
      val f = CassandraSource(boundStmt).runWith(Sink.seq)
      val results = Await.result(f, 60.seconds)
      results.foreach(row => println(row.getObject(1) + " : " + row))
      println("INSERST size: " + results.size)
    }

    "SELECT" in {
      val stmt = new SimpleStatement("select * from akka_stream_scala_test.test")
      val rowsFuture = CassandraSource(stmt).runWith(Sink.seq)
      val rows = Await.result(rowsFuture, 60.seconds)
      rows.foreach(row => println(row))
    }
  }

}
