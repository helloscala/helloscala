package helloscala.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;

@FunctionalInterface
public interface ConnectionPreparedStatementCreator {
    PreparedStatement apply(Connection conn);
}
