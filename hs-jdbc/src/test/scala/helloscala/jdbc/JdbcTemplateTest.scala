package helloscala.jdbc

import java.util.Properties

import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import helloscala.test.HelloscalaSpec
import org.scalatest.BeforeAndAfterAll

class JdbcTemplateTest extends HelloscalaSpec with BeforeAndAfterAll {

  val dataSource = {
    val props = new Properties()
    props.put("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
    props.put("dataSource.serverName", "127.0.0.1")
    props.put("dataSource.portNumber", "5432")
    props.put("dataSource.databaseName", "hldev")
    props.put("dataSource.user", "hldev")
    props.put("dataSource.password", "hldev")
    new HikariDataSource(new HikariConfig())
  }

  val jdbcTemplate = new JdbcTemplate(dataSource, true, true, true)
  "JdbcTemplate" should {

    "create table test_table" in {
      val sql =
        """
          |CREATE TABLE test_table(
          |  id BIGSERIAL,
          |  name VARCHAR(255),
          |  age INT,
          |  PRIMARY KEY (id)
          |);
        """.stripMargin

      val result = jdbcTemplate.execute(sql)
      result mustBe false
    }

    "insert into test_table" in {
      val sql = "INSERT INTO test_table(name, age) VALUES('羊八井', 31), ('杨景', 31);"
      val result = jdbcTemplate.update(sql, Nil)
      println(s"result: $result")
      result must be > 0
    }

    "select from test_table" in {
      val sql = "SELECT * FROM test_table WHERE id = 2"
      val results = jdbcTemplate.listForMap(sql, Nil)
      results must have length 1
      println(results)
    }

    "select from test_table list" in {
      val sql = "SELECT * FROM test_table LIMIT 10"
      val list = jdbcTemplate.listForObject(
        sql,
        Nil,
        rs => TestTable(rs.getLong("id"), rs.getString("name"), rs.getInt("age")))
      list.foreach(println)
      list.size must be > 0
      list.size must be < 10
    }

    "insert by Map" in {
      val sql = "INSERT INTO test_table(name, age) VALUES(?name, ?age)"
      val result = jdbcTemplate.namedUpdate(sql, Map("name" -> "yangjing", "age" -> 31))
      result mustBe 31
    }
  }

  override protected def afterAll(): Unit = {
    dataSource.close()
  }
}

case class TestTable(id: Long, name: String, age: Int)
