package helloscala.nosql.cassandra

import com.datastax.driver.core.Session
import helloscala.test.{AkkaSpec, HelloscalaSpec}

trait CassandraSpec extends HelloscalaSpec with AkkaSpec {
  implicit val session: Session

  override protected def afterAll(): Unit = {
    session.close()
    super.afterAll()
  }
}
