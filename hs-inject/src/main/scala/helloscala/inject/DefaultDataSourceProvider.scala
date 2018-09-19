/*
 * Copyright 2017 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helloscala.inject

import java.util.Properties
import javax.inject.{Inject, Singleton}
import javax.sql.DataSource

import com.google.inject.Provider
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import helloscala.common.Configuration
import helloscala.jdbc.JdbcUtils.Keys
import helloscala.jdbc.{JdbcTemplate, JdbcUtils}

@Singleton
class DefaultDataSourceProvider @Inject()(configuration: Configuration) extends Provider[HikariDataSource] {

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
class DefaultJdbcTemplateProvider @Inject()(dataSource: DataSource, configuration: Configuration)
    extends Provider[JdbcTemplate] {

  private[this] val jdbcTemplate = new JdbcTemplate(
    dataSource,
    configuration
      .get[Option[Boolean]](JdbcUtils.DATASOURCE_PATH + "." + Keys.USE_TRANSACTION)
      .getOrElse(true),
    configuration
      .get[Option[Boolean]](JdbcUtils.DATASOURCE_PATH + "." + Keys.IGNORE_WARNINGS)
      .getOrElse(true),
    configuration
      .get[Option[Boolean]](JdbcUtils.DATASOURCE_PATH + "." + Keys.ALLOW_PRINT_LOG)
      .getOrElse(true)
  )

  override def get(): JdbcTemplate = jdbcTemplate

}
