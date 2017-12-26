package helloscala.inject

import java.util.Properties
import javax.inject.{ Inject, Singleton }
import javax.sql.DataSource

import com.google.inject.Provider
import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import helloscala.common.Configuration
import helloscala.jdbc.JdbcUtils.Keys
import helloscala.jdbc.{ JdbcTemplate, JdbcUtils }

@Singleton
class DefaultDataSourceProvider @Inject() (configuration: Configuration) extends Provider[HikariDataSource] {

  private[this] def properties = {
    val props = configuration.get[Properties](JdbcUtils.DATASOURCE_PATH)
    for (key <- Keys.keys) {
      props.remove(key)
    }
    props
  }

  private[this] val dataSource = new HikariDataSource(new HikariConfig(properties))

  override def get(): HikariDataSource = dataSource

}

@Singleton
class DefaultJdbcTemplateProvider @Inject() (
  dataSource: DataSource,
  configuration: Configuration) extends Provider[JdbcTemplate] {

  private[this] val jdbcTemplate = new JdbcTemplate(
    dataSource,
    configuration.get[Option[Boolean]](JdbcUtils.DATASOURCE_PATH + "." + Keys.USE_TRANSACTION).getOrElse(true),
    configuration.get[Option[Boolean]](JdbcUtils.DATASOURCE_PATH + "." + Keys.IGNORE_WARNINGS).getOrElse(true),
    configuration.get[Option[Boolean]](JdbcUtils.DATASOURCE_PATH + "." + Keys.ALLOW_PRINT_LOG).getOrElse(true))

  override def get(): JdbcTemplate = jdbcTemplate

}
