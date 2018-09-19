package helloscala.common

import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-06.
 */
class ConfigurationTest extends WordSpec with MustMatchers with BeforeAndAfterAll {
  private var config: Configuration = _

  "HlConfigurationTest" should {

    "get" in {
      val version = config.get[String]("helloscala.version")
      version mustBe "0.0.9"
    }

    "properties" in {
      val c = ConfigFactory
        .parseString("""datasource {
            |  dataSourceDriverClass = "org.postgresql.JDBC"
            |  dataSource {
            |    username = "username"
            |    password = "password"
            |    server = "127.0.0.1"
            |    port = 5432
            |  }
            |}""".stripMargin)
      val cc = Configuration(c)

      val dm = cc.get[Properties]("datasource")
      println(dm)
    }

    "entrySet" in {}

    "keys" in {}

    "getNanos" in {}

    "has" in {}

    "getOptional" in {}

    "getMillis" in {}

    "subKeys" in {}

  }

  override protected def beforeAll(): Unit = {
    config = Configuration()
  }
}
