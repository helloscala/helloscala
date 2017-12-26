package helloscala.jdbc

import java.sql.PreparedStatement

class PreparedStatementActionImpl[R](val args: Iterable[Any], func: PreparedStatement => R) extends PreparedStatementAction[R] {
  override def apply(pstmt: PreparedStatement): R = func(pstmt)
}
