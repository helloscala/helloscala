package helloscala.jdbc

import helloscala.test.HelloscalaSpec

class JdbcUtilsTest extends HelloscalaSpec {

  "JdbcUtils" should {

    "toCountSql" in {
      JdbcUtils.toCountSql(
        "SELECT * FROM test WHERE a = ? ORDER BY a asc") mustBe "SELECT count(*) FROM test WHERE a = ?"

      JdbcUtils.toCountSql(
        "SELECT * FROM test WHERE a = ? LIMIT 10") mustBe "SELECT count(*) FROM test WHERE a = ?"

      JdbcUtils.toCountSql(
        "SELECT * FROM test WHERE a = ? offset 10") mustBe "SELECT count(*) FROM test WHERE a = ?"

      JdbcUtils.toCountSql(
        "SELECT * FROM test WHERE a = ? group by a") mustBe "SELECT count(*) FROM test WHERE a = ?"
    }

    "whereOption" in {
      JdbcUtils.whereOption(Seq()) mustBe ""
    }

    "generateWhere" in {
      JdbcUtils.generateWhere(Seq()) mustBe ""
      JdbcUtils.generateWhere(Seq(None)) mustBe ""
      JdbcUtils.generateWhere(Seq(Some("a = ?"))) mustBe " WHERE a = ? "
      JdbcUtils.generateWhere(Seq("a = ?")) mustBe " WHERE a = ? "
      JdbcUtils.generateWhere(Seq(None, Some("a = ?"))) mustBe " WHERE a = ? "
      JdbcUtils.generateWhere(Seq(Some("b = ?"), "c = ?")) mustBe " WHERE b = ? AND c = ? "
      JdbcUtils.generateWhere(
        Seq("a = ?", "b = ?", Some("c = ?"), None, Some("d = ?"))) mustBe " WHERE a = ? AND b = ? AND c = ? AND d = ? "
    }

    "namedParameterToQuestionMarked" in {
      val (qmSql, params) = JdbcUtils.namedParameterToQuestionMarked("INSERT INTO test(name, age) VALUES(?name,?age)")
      qmSql mustBe "INSERT INTO test(name, age) VALUES(?,?)"
      println(s"params: $params")
    }
  }

}
