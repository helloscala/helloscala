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

package helloscala.nosql.cassandra

import java.util.Optional

import helloscala.common.Configuration

import scala.compat.java8.OptionConverters._

/**
 * Cassandra 配置
 *
 * @param nodes       节点
 * @param clusterName 集群名字
 * @param localDc     本地数据中心名字
 * @param username    用户名
 * @param password    密码
 * @param keyspace    默认keyspace名字
 */
case class CassandraConf(
    nodes: Array[String],
    clusterName: String,
    localDc: Option[String],
    username: Option[String],
    password: Option[String],
    keyspace: Option[String]) {

  def javaUsername: Optional[String] = username.asJava

  def javaPassword: Optional[String] = password.asJava

  def javaLocalDc: Optional[String] = localDc.asJava
}

object CassandraConf {

  def apply(config: Configuration, path: String): CassandraConf =
    apply(config.get[Configuration](path))

  def apply(config: Configuration): CassandraConf =
    CassandraConf(
      config.get[Array[String]]("nodes"),
      config.get[String]("cluster-name"),
      config.get[Option[String]]("local-dc"),
      config.get[Option[String]]("username"),
      config.get[Option[String]]("password"),
      config.get[Option[String]]("keyspace")
    )

}
