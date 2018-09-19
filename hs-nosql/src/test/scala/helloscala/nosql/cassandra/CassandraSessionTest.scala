package helloscala.nosql.cassandra

import com.datastax.driver.core.Cluster
import com.typesafe.config.ConfigFactory
import helloscala.common.Configuration
import helloscala.test.HelloscalaSpec
import org.scalatest.BeforeAndAfterAll

class StandaloneCassandraSession(val conf: CassandraConf) extends CassandraSession {
  override val cluster: Cluster = CassandraHelper.getCluster(conf)
}

class CassandraSessionTest extends HelloscalaSpec with BeforeAndAfterAll {

  val cassandraSession: CassandraSession =
    new StandaloneCassandraSession(
      CassandraConf(Configuration(ConfigFactory.parseString("""nodes = ["dn126"]
        |cluster-name = "Test Cluster"
        |username = "devops"
        |password = "hl.Data2017"
        |keyspace = "recommend_system"""".stripMargin))))

  override protected def afterAll(): Unit = {
    cassandraSession.cluster.close()
  }

  "CassandraSession" should {
    "create table" in {
      val sql =
        """CREATE TABLE test(
          |id INT PRIMARY KEY,
          |name TEXT,
          |created_at TIMESTAMP
          |);""".stripMargin
      cassandraSession.execute(sql)
    }

    "insert" in {
      val sql = ""
      cassandraSession.executeAsync(sql)
    }

    "select" in {}

    "delete" in {}
  }

}
