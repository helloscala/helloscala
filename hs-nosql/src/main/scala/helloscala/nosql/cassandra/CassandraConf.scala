/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-22.
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

  def apply(config: Configuration, path: String): CassandraConf = apply(config.get[Configuration](path))

  def apply(config: Configuration): CassandraConf =
    CassandraConf(
      config.get[Array[String]]("nodes"),
      config.get[String]("cluster-name"),
      config.get[Option[String]]("local-dc"),
      config.get[Option[String]]("username"),
      config.get[Option[String]]("password"),
      config.get[Option[String]]("keyspace"))

}
