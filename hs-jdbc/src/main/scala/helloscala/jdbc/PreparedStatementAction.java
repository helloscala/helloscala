package helloscala.jdbc;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface PreparedStatementAction<T> {
    T apply(PreparedStatement pstmt);
}
