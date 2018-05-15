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

package helloscala.jdbc

import java.lang.reflect.{Field, Modifier}
import java.sql._
import java.time._
import java.util.{Properties, HashMap => JHashMap, Map => JMap}

import com.typesafe.scalalogging.StrictLogging
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import helloscala.common.HSCommons
import helloscala.common.util._

import scala.collection.mutable
import scala.compat.java8.FunctionConverters._
import scala.reflect.ClassTag

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-07-27.
 */
object JdbcUtils extends StrictLogging {

  val DATASOURCE_PATH = HSCommons.PERSISTENCE_PATH + ".datasource"

  val AND = "AND"
  val OR = "OR"

  /**
   * 用户限制返回记录条数时说明不做限制
   */
  val INF_LIMIT = -1

  /**
   * Constant that indicates an unknown (or unspecified) SQL type.
   *
   * @see java.sql.Types
   */
  val TYPE_UNKNOWN = Integer.MIN_VALUE

  val BeanIgnoreClass = classOf[BeanIgnore]

  object Keys {
    val USE_TRANSACTION = "useTransaction"
    val IGNORE_WARNINGS = "ignoreWarnings"
    val ALLOW_PRINT_LOG = "allowPrintLog"
    val NUM_THREADS = "numThreads"
    val MAX_CONNECTIONS = "maxConnections"

    val keys = List(USE_TRANSACTION, IGNORE_WARNINGS, ALLOW_PRINT_LOG, NUM_THREADS, MAX_CONNECTIONS)
  }

  // Check for JDBC 4.1 getObject(int, Class) method - available on JDK 7 and higher
  private val getObjectWithTypeAvailable =
    ClassUtils.hasMethod(classOf[ResultSet], "getObject", classOf[Int], classOf[Class[_]])

  /**
   * 将所有 :name 命名参数替换成?
   * @param sql 采用命名参数编写的SQL语句
   * @return (转换后SQL语句，提取出的参数和索引)，索引从1开始编号
   */
  def namedParameterToQuestionMarked(sql: String): (String, Map[String, Int]) = {
    val sqlBuf = mutable.Buffer.empty[Char]
    val paramBuf = mutable.Buffer.empty[Char]
    val params = mutable.Map.empty[String, Int]
    var idx = 0
    var isName = false
    sql.foreach {
      case '?' =>
        sqlBuf.append('?')
        isName = true
      case c @ (',' | ')') if isName =>
        sqlBuf.append(c)
        idx += 1
        params += (paramBuf.mkString.trim -> idx)
        paramBuf.clear()
        isName = false
      case c if isName =>
        paramBuf.append(c)
      case c =>
        sqlBuf.append(c)
    }
    (sqlBuf.mkString, params.toMap)
  }

  def generateWhere(wheres: Seq[AnyRef], step: String = AND): String = {
    val list = wheres.flatMap {
      case Some(partial) => Some(partial)
      case None          => None
      case partial       => Some(partial)
    }
    if (list.isEmpty) "" else list.mkString(" WHERE ", s" $step ", " ")
  }

  def where(wheres: Seq[String], step: String = AND): String =
    if (wheres.isEmpty) "" else wheres.mkString(" WHERE ", s" $step ", " ")

  def whereOption(wheres: Seq[Option[String]], step: String = AND): String = where(wheres.flatten, step)

  def toCountSql(sql: String): String = {
    def indexOf(text: String, str: String): Int = {
      val idx = text.indexOf(str.toUpperCase())
      if (idx < 0) text.indexOf(str.toLowerCase()) else idx
    }

    def takeString(text: String, str: String): String = {
      val idx = indexOf(text, str)
      if (idx > 0) text.substring(0, idx) else text
    }

    var countSql = sql.trim

    countSql = takeString(countSql, "order")
    countSql = takeString(countSql, "limit")
    countSql = takeString(countSql, "offset")
    countSql = takeString(countSql, "group")

    val idxFrom = indexOf(countSql, "from")

    countSql.substring(0, 6) + " count(*) " + countSql.drop(idxFrom).trim
  }

  /**
   * 用户限制返回记录条数时说明不做限制
   * @param limit 限制记录条数
   * @return limit < 1：将不限制返回记录条数，反之将限制返回记录条数
   */
  def isInfLimit(limit: Int): Boolean = limit < 1

  def nonInfLimit(limit: Int): Boolean = !isInfLimit(limit)

  def nonInfLimit(limit: Int, additionalSql: => String): String = if (isInfLimit(limit)) "" else additionalSql

  def resultSetToMap(rs: ResultSet): Map[String, Object] = {
    val metaData = rs.getMetaData
    (1 to metaData.getColumnCount).map { column =>
      val label = metaData.getColumnLabel(column)
      label -> getResultSetValue(rs, column) //rs.getObject(label)
    }.toMap
  }

  def resultSetToJMap(rs: ResultSet): JMap[String, Object] = {
    val result = new JHashMap[String, Object]()
    val metaData = rs.getMetaData
    (1 to metaData.getColumnCount)
      .foreach { column =>
        val label = metaData.getColumnLabel(column)
        result.put(label, getResultSetValue(rs, column) /*rs.getObject(label)*/ )
      }
    result
  }

  private def filterFields(fields: scala.Array[Field]): Map[String, Field] = {
    val result = mutable.Map.empty[String, Field]
    val len = fields.length
    var i = 0
    while (i < len) {
      val field = fields(i)
      val anns = field.getDeclaredAnnotations
      val isInvalid = Modifier.isStatic(field.getModifiers) ||
        anns.exists(ann => ann.annotationType() == BeanIgnoreClass)
      if (!isInvalid) {
        field.setAccessible(true)
        result.put(field.getName, field)
      }
      i += 1
    }
    result.toMap
  }

  def resultSetToBean[T](rs: ResultSet)(implicit ev1: ClassTag[T]): T = resultSetToBean(rs, toPropertiesName = true)

  def resultSetToBean[T](rs: ResultSet, toPropertiesName: Boolean)(implicit ev1: ClassTag[T]): T = {
    val dest = ev1.runtimeClass.newInstance().asInstanceOf[T]
    val cls = dest.getClass
    val fields = filterFields(cls.getDeclaredFields)
    val metaData = rs.getMetaData
    var col = 1
    val columnCount = metaData.getColumnCount
    while (col <= columnCount) {
      var label = metaData.getColumnLabel(col)
      if (toPropertiesName) {
        label = convertUnderscoreNameToPropertyName(label)
      }
      for (field <- fields.get(label)) {
        val requiredType = field.getType
        val value = getResultSetValue(rs, col, requiredType)
        field.set(dest, value)
      }
      col += 1
    }
    dest
  }

  def preparedStatementCreator(sql: String, namedSql: String = ""): ConnectionPreparedStatementCreator =
    new ConnectionPreparedStatementCreatorImpl(sql, namedSql)

  def preparedStatementAction[R](args: Iterable[Any], func: PreparedStatement => R): PreparedStatementAction[R] =
    new PreparedStatementActionImpl(args, func)

  def preparedStatementActionUseUpdate(args: Iterable[Any]): PreparedStatementAction[Int] =
    new PreparedStatementActionImpl(
      args,
      pstmt => {
        setStatementParameters(pstmt, args)
        pstmt.executeUpdate()
      })

  def preparedStatementActionUseUpdate(args: Map[String, Any], paramIndex: Map[String, Int]): PreparedStatementAction[Int] =
    new PreparedStatementActionImpl(
      args,
      pstmt => {
        for ((param, index) <- paramIndex) {
          setParameter(pstmt, index, args(param))
        }
        pstmt.executeUpdate()
      })

  def preparedStatementActionUseBatchUpdate(argsList: Iterable[Iterable[Any]]): PreparedStatementAction[scala.Array[Int]] =
    new PreparedStatementActionImpl(
      argsList,
      pstmt => {
        for (args <- argsList) {
          setStatementParameters(pstmt, args)
          pstmt.addBatch()
        }
        pstmt.executeBatch()
      })

  def preparedStatementActionUseBatchUpdate(argsList: Iterable[Map[String, Any]], paramIndex: Map[String, Int]): PreparedStatementAction[scala.Array[Int]] =
    new PreparedStatementActionImpl(
      argsList,
      pstmt => {
        for (args <- argsList) {
          for ((param, index) <- paramIndex) {
            setParameter(pstmt, index, args(param))
          }
          pstmt.addBatch()
        }
        pstmt.executeBatch()
      })

  def setStatementParameters(pstmt: PreparedStatement, args: Map[String, Any], paramIndex: Map[String, Int]): PreparedStatement = {
    for ((param, index) <- paramIndex) {
      setParameter(pstmt, index, args(param))
    }
    pstmt
  }

  def setStatementParameters(pstmt: PreparedStatement, args: Iterable[Any]): PreparedStatement = {
    var i = 0
    for (arg <- args) {
      i += 1
      setParameter(pstmt, i, arg)
    }
    pstmt
  }

  private val JAVA_UTIL_DATE_NAME = "java.util.Date"

  def setParameter(pstmt: PreparedStatement, i: Int, arg: Any): Unit = {
    val obj = arg match {
      case zdt: ZonedDateTime => Timestamp.from(zdt.toInstant)
      case ldt: LocalDateTime => Timestamp.valueOf(ldt)
      case ld: LocalDate => Date.valueOf(ld)
      case odt: OffsetDateTime => Timestamp.from(odt.toInstant)
      case t: LocalTime => java.sql.Time.valueOf(t)
      case e: Enumeration#Value => e.id
      case d: java.util.Date if d.getClass.getName == JAVA_UTIL_DATE_NAME => new Date(d.getTime)
      case _ => arg
    }
    pstmt.setObject(i, obj)
  }

  def closeStatement(stmt: Statement): Unit =
    if (stmt ne null) {
      try stmt.close()
      catch {
        case ex: SQLException =>
          logger.trace("Could not close JDBC Statement", ex)
        case ex: Throwable =>
          // We don't trust the JDBC driver: It might throw RuntimeException or Error.
          logger.trace("Unexpected exception on closing JDBC Statement", ex)
      }
    }

  def closeResultSet(rs: ResultSet): Unit =
    if (rs != null) {
      try rs.close()
      catch {
        case ex: SQLException =>
          logger.trace("Could not close JDBC ResultSet", ex)
        case ex: Throwable =>
          // We don't trust the JDBC driver: It might throw RuntimeException or Error.
          logger.trace("Unexpected exception on closing JDBC ResultSet", ex)
      }
    }

  def closeConnection(con: Connection): Unit =
    if (con != null) {
      try con.close()
      catch {
        case ex: SQLException =>
          logger.error("Could not close JDBC Connection", ex)
        case ex: Throwable =>
          // We don't trust the JDBC driver: It might throw RuntimeException or Error.
          logger.error("Unexpected exception on closing JDBC Connection", ex)
      }
    }

  def getResultSetValue(
      rs: ResultSet,
      index: Int,
      requiredType: Class[_],
      defaultTimeZone: ZoneOffset = TimeUtils.ZONE_CHINA_OFFSET): Any =
    if (requiredType == null) {
      getResultSetValue(rs, index)
    } else if (classOf[String] == requiredType) {
      rs.getString(index)
    } else if (classOf[BigDecimal] == requiredType) {
      rs.getBigDecimal(index)
    } else if (classOf[java.sql.Timestamp] == requiredType) {
      rs.getTimestamp(index)
    } else if (classOf[java.sql.Date] == requiredType) {
      rs.getDate(index)
    } else if (classOf[LocalDate] == requiredType) {
      rs.getDate(index).toLocalDate
    } else if (classOf[LocalTime] == requiredType) {
      rs.getTime(index).toLocalTime
    } else if (classOf[ZonedDateTime] == requiredType) {
      rs.getTimestamp(index).toInstant.atZone(defaultTimeZone)
    } else if (classOf[LocalDateTime] == requiredType) {
      rs.getTimestamp(index).toLocalDateTime
    } else if (classOf[java.sql.Time] == requiredType) {
      rs.getTime(index)
    } else if (classOf[scala.Array[Byte]] == requiredType) {
      rs.getBytes(index)
    } else if (classOf[Blob] == requiredType) {
      rs.getBlob(index)
    } else if (classOf[Clob] == requiredType) {
      rs.getClob(index)
    } else if (requiredType.isEnum) {
      rs.getObject(index) match {
        case s: String => s
        case n: Number => NumberUtils.convertNumberToTargetClass(n, classOf[Integer])
        case _         => rs.getString(index)
      }
    } else {
      var value: Any = null
      if (classOf[Boolean] == requiredType || classOf[java.lang.Boolean] == requiredType) {
        value = rs.getBoolean(index)
      } else if (classOf[Byte] == requiredType || classOf[java.lang.Byte] == requiredType) {
        value = rs.getByte(index)
      } else if (classOf[Short] == requiredType || classOf[java.lang.Short] == requiredType) {
        value = rs.getShort(index)
      } else if (classOf[Int] == requiredType || classOf[Integer] == requiredType) {
        value = rs.getInt(index)
      } else if (classOf[Long] == requiredType || classOf[java.lang.Long] == requiredType) {
        value = rs.getLong(index)
      } else if (classOf[Float] == requiredType || classOf[java.lang.Float] == requiredType) {
        value = rs.getFloat(index)
      } else if (classOf[Double] == requiredType || classOf[java.lang.Double] == requiredType || classOf[Number] == requiredType) {
        value = rs.getDouble(index)
      } else {
        // Some unknown type desired -> rely on getObject.
        if (getObjectWithTypeAvailable) {
          try value = rs.getObject(index, requiredType)
          catch {
            case err: AbstractMethodError =>
              logger.debug("JDBC driver does not implement JDBC 4.1 'getObject(int, Class)' method", err)
            case ex: SQLFeatureNotSupportedException =>
              logger.debug("JDBC driver does not support JDBC 4.1 'getObject(int, Class)' method", ex)
            case ex: SQLException =>
              logger.debug("JDBC driver has limited support for JDBC 4.1 'getObject(int, Class)' method", ex)
          }
          //        } else {
          //          // Corresponding SQL types for JSR-310, left up
          //          // to the caller to convert them (e.g. through a ConversionService).
          //          val typeName = requiredType.getSimpleName
          //          value = typeName match {
          //            case "ZonedDateTime" => rs.getTimestamp(index).toInstant.atZone(TimeUtils.ZONE_CHINA_OFFSET)
          //            case "LocalDateTime" => rs.getTimestamp(index).toLocalDateTime
          //            case "LocalDate"     => rs.getDate(index).toLocalDate
          //            case "LocalTime"     => rs.getTime(index).toLocalTime
          //            case _ =>
          //              // Fall back to getObject without type specification, again
          //              // left up to the caller to convert the value if necessary.
          //              getResultSetValue(rs, index)
          //          }
        }
      }

      if (rs.wasNull()) null else value
    }

  /**
   * Retrieve a JDBC column value from a ResultSet, using the most appropriate
   * value type. The returned value should be a detached value object, not having
   * any ties to the active ResultSet: in particular, it should not be a Blob or
   * Clob object but rather a byte array or String representation, respectively.
   * <p>Uses the {@code getObject(index)} method, but includes additional "hacks"
   * to get around Oracle 10g returning a non-standard object for its TIMESTAMP
   * datatype and a {@code java.sql.Date} for DATE columns leaving out the
   * time portion: These columns will explicitly be extracted as standard
   * {@code java.sql.Timestamp} object.
   *
   * @param rs    is the ResultSet holding the data
   * @param index is the column index
   * @return the value object
   * @throws SQLException if thrown by the JDBC API
   * @see java.sql.Blob
   * @see java.sql.Clob
   * @see java.sql.Timestamp
   */
  @throws[SQLException]
  def getResultSetValue(rs: ResultSet, index: Int): AnyRef = {
    val obj = rs.getObject(index)
    val className: String = if (obj == null) null else obj.getClass.getName

    obj match {
      case null =>
        null
      case blob: Blob =>
        blob.getBytes(1, blob.length().toInt)
      case clob: Clob =>
        clob.getSubString(1, clob.length().toInt)
      case _ if "oracle.sql.TIMESTAMP" == className || "oracle.sql.TIMESTAMPTZ" == className =>
        rs.getTimestamp(index)
      case _ if className.startsWith("oracle.sql.DATE") =>
        val metaDataClassName = rs.getMetaData.getColumnClassName(index)
        if ("java.sql.Timestamp" == metaDataClassName || "oracle.sql.TIMESTAMP" == metaDataClassName)
          rs.getTimestamp(index)
        else
          rs.getDate(index)
      case _: Date if "java.sql.Timestamp" == rs.getMetaData.getColumnClassName(index) =>
        rs.getTimestamp(index)
      case other =>
        other
    }
  }

  def supportsBatchUpdates(con: Connection): Boolean = {
    var b = false
    try {
      val dbmd = con.getMetaData
      if (dbmd != null) {
        if (dbmd.supportsBatchUpdates) {
          logger.debug("JDBC driver supports batch updates")
          b = true
        } else {
          logger.debug("JDBC driver does not support batch updates")
        }
      }
    } catch {
      case ex: SQLException =>
        logger.debug("JDBC driver 'supportsBatchUpdates' method threw exception", ex)
    }
    b
  }

  /**
   * Extract a common name for the database in use even if various drivers/platforms provide varying names.
   *
   * @param source the name as provided in database metadata
   * @return the common name to be used
   */
  def commonDatabaseName(source: String): String = {
    var name = source
    if (source != null && source.startsWith("DB2")) {
      name = "DB2"
    } else if ("Sybase SQL Server" == source || "Adaptive Server Enterprise" == source || "ASE" == source ||
      "sql server".equalsIgnoreCase(source)) {
      name = "Sybase"
    }
    name
  }

  /**
   * Check whether the given SQL type is numeric.
   *
   * @param sqlType the SQL type to be checked
   * @return whether the type is numeric
   */
  def isNumeric(sqlType: Int): Boolean =
    Types.BIT == sqlType || Types.BIGINT == sqlType || Types.DECIMAL == sqlType || Types.DOUBLE == sqlType ||
      Types.FLOAT == sqlType || Types.INTEGER == sqlType || Types.NUMERIC == sqlType || Types.REAL == sqlType ||
      Types.SMALLINT == sqlType || Types.TINYINT == sqlType

  /**
   * Determine the column name to use. The column name is determined based on a
   * lookup using ResultSetMetaData.
   * <p>This method implementation takes into account recent clarifications
   * expressed in the JDBC 4.0 specification:
   * <p><i>columnLabel - the label for the column specified with the SQL AS clause.
   * If the SQL AS clause was not specified, then the label is the name of the column</i>.
   *
   * @return the column name to use
   * @param resultSetMetaData the current meta data to use
   * @param columnIndex       the index of the column for the look up
   * @throws SQLException in case of lookup failure
   */
  @throws[SQLException]
  def lookupColumnName(resultSetMetaData: ResultSetMetaData, columnIndex: Int): String = {
    var name = resultSetMetaData.getColumnLabel(columnIndex)
    if (name == null || name.length < 1) {
      name = resultSetMetaData.getColumnName(columnIndex)
    }
    name
  }

  def convertPropertyNameToUnderscore(obj: JMap[String, Object]): JMap[String, Object] =
    convertPropertyNameToUnderscore(obj, true)

  def convertPropertyNameToUnderscore(obj: JMap[String, Object], isLower: Boolean): JMap[String, Object] = {
    val result = new JHashMap[String, Object]()
    val func: (String, Object) => Unit = (key, value) =>
      result.put(convertPropertyNameToUnderscore(key, isLower), value)
    obj.forEach(asJavaBiConsumer(func))
    result
  }

  def convertPropertyNameToUnderscore(obj: Map[String, Any]): Map[String, Any] =
    convertPropertyNameToUnderscore(obj, true)

  def convertPropertyNameToUnderscore(obj: Map[String, Any], isLower: Boolean): Map[String, Any] =
    obj.map { case (key, value) => convertPropertyNameToUnderscore(key, isLower) -> value }

  /**
   * 字符串从属性形式转换为全小写的下划线形式
   *
   * @param name 待转字符串
   * @see convertPropertyNameToUnderscore(name: String, isLower: Boolean)
   * @return
   */
  def convertPropertyNameToUnderscore(name: String): String = convertPropertyNameToUnderscore(name, isLower = true)

  /**
   * 字符串从属性形式转换为下划线形式
   *
   * @param name    待转字符串
   * @param isLower 转换成下划线形式后是否使用小写，false将完全使用大写
   * @return 转换后字符串
   */
  def convertPropertyNameToUnderscore(name: String, isLower: Boolean): String =
    if (StringUtils.isBlank(name)) {
      name
    } else {
      val sb = new StringBuilder
      for (c <- name) {
        if (Character.isUpperCase(c)) {
          sb.append('_')
        }
        sb.append(if (isLower) Character.toLowerCase(c) else Character.toUpperCase(c.toUpper))
      }
      sb.toString()
    }

  def convertUnderscoreNameToPropertyName(obj: Map[String, Any]): Map[String, Any] =
    obj.map { case (key, value) => convertUnderscoreNameToPropertyName(key) -> value }

  def convertUnderscoreNameToPropertyName(obj: JMap[String, Object]): JMap[String, Object] = {
    val result = new JHashMap[String, Object]()
    val func: (String, Object) => Unit = (key, value) => result.put(convertUnderscoreNameToPropertyName(key), value)
    obj.forEach(asJavaBiConsumer(func))
    result
  }

  /**
   * Convert a column name with underscores to the corresponding property name using "camel case".  A name
   * like "customer_number" would match a "customerNumber" property name.
   *
   * @param name the column name to be converted
   * @return the name using "camel case"
   */
  def convertUnderscoreNameToPropertyName(name: String): String = {
    val result = new StringBuilder
    var nextIsUpper = false
    if (name != null && name.length > 0) {
      if (name.length > 1 && name.substring(1, 2) == "_") {
        result.append(name.substring(0, 1).toUpperCase)
      } else {
        result.append(name.substring(0, 1).toLowerCase)
      }

      var i = 1
      val len = name.length
      while (i < len) {
        val s = name.substring(i, i + 1)
        if (s == "_") {
          nextIsUpper = true
        } else if (nextIsUpper) {
          result.append(s.toUpperCase)
          nextIsUpper = false
        } else {
          result.append(s.toLowerCase)
        }

        i += 1
      }
    }
    result.toString
  }

  def createHikariDataSource(props: Properties): HikariDataSource = {
    val config = new HikariConfig(props)
    createHikariDataSource(config)
  }

  def createHikariDataSource(config: HikariConfig): HikariDataSource =
    new HikariDataSource(config)

}
