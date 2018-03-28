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

package helloscala.slick

import javax.sql.DataSource

import helloscala.common.{AppLifecycle, Configuration}

import scala.concurrent.Future

trait SchemasTrait {
  val profile: PgProfile.type = PgProfile

  import profile.api._

  val dataSource: DataSource
  val appLifecycle: AppLifecycle
  val configuration: Configuration

  protected val confMaxConnectionsPath = "helloscala.persistence.datasource.maxConnections"
  protected val confKeepAliveConnectionPath = "helloscala.persistence.datasource.keepAliveConnection"
  protected val confNumThreadsPath = "helloscala.persistence.datasource.numThreads"
  protected val confQueueSizePath = "helloscala.persistence.datasource.queueSize"

  val db = Database.forDataSource(
    dataSource,
    configuration.get[Option[Int]](confMaxConnectionsPath),
    executor = AsyncExecutor(
      "helloscala",
      configuration.get[Option[Int]](confNumThreadsPath).getOrElse(20),
      configuration.get[Option[Int]](confQueueSizePath).getOrElse(1000)),
    keepAliveConnection = configuration.get[Option[Boolean]](confKeepAliveConnectionPath).getOrElse(false))
  appLifecycle.addStopHook(() => Future.successful(db.close()))
}
