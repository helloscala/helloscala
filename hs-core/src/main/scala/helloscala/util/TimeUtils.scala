/**
 * 时间、日期工具函数
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-08.
 */
package helloscala.util

import java.sql.{ Date => SQLDate, Time => SQLTime, Timestamp => SQLTimestamp }
import java.time._
import java.time.format.DateTimeFormatter
import java.util.Date

import com.typesafe.scalalogging.StrictLogging

import scala.util.Try

class TimeUtils extends Serializable with StrictLogging {

  val DATE_TIME_EPOCH: LocalDateTime = LocalDateTime.of(1970, 1, 1, 0, 0, 0)
  val ZONE_CHINA_OFFSET: ZoneOffset = ZoneOffset.ofHours(8)
  val formatterDateTime: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val formatterMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
  val formatterDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
  val formatterDateHours: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
  val formatterDateMinutes: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  val formatterMinutes: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
  val formatterTime: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

  def now(): LocalDateTime = LocalDateTime.now()

  def nowTimestamp(): SQLTimestamp = SQLTimestamp.from(Instant.now())

  /**
   * 解析字符串为 LocalDate
   *
   * @param date 字符串形式日期
   * @return 成功返回 LocalDate
   */
  def toLocalDate(date: String): LocalDate =
    Try(LocalDate.parse(date, formatterDate)) getOrElse {
      val (year, month, day) = date.split("""[-:]""") match {
        case Array(y, m, d) =>
          (y.toInt, m.toInt, d.toInt)
        case Array(y, m) =>
          (y.toInt, m.toInt, 1)
        case Array(y) =>
          (y.toInt, 1, 1)
        case _ =>
          throw new IllegalArgumentException(s"$date is invalid iso date format")
      }

      if (year < 0 || year > 9999)
        throw new IllegalArgumentException(s"$date is invalid iso date format ($year)")

      LocalDate.of(year, month, day)
    }

  /**
   * 解析字符串为 LocalTime
   *
   * @param time 字符串形式时间
   * @return 成功返回 LocalTime
   */
  def toLocalTime(time: String): LocalTime =
    Try(LocalTime.parse(time, formatterTime)) getOrElse {
      val (hour, minute, second, nano) =
        time.split("""[:-]""") match {
          case Array(h, m, s) =>
            s.split('.') match {
              case Array(sec, millis) =>
                (h.toInt, m.toInt, sec.toInt, millis.toInt * 1000 * 1000)
              case arr =>
                (h.toInt, m.toInt, arr(0).toInt, 0)
            }
          case Array(h, m) =>
            (h.toInt, m.toInt, 0, 0)
          case Array(h) =>
            (h.toInt, 0, 0, 0)
          case _ =>
            throw new IllegalArgumentException(s"$time is invalid iso time format")
        }

      LocalTime.of(hour, minute, second, nano)
    }

  /**
   * 解析字符串为 LocalDateTime
   *
   * @param date 字符串形式日期
   * @param time 字符串形式时间
   * @return 成功返回 LocalDateTime
   */
  def toLocalDateTime(date: String, time: String): LocalDateTime = {
    val d = toLocalDate(date)
    val t = toLocalTime(time)
    LocalDateTime.of(d, t)
  }

  /**
   * 解析字符串为 LocalDateTime
   *
   * @param datetime 字符串形式日期时间
   * @return 成功返回 LocalDateTime
   */
  def toLocalDateTime(datetime: String): LocalDateTime =
    Try(LocalDateTime.parse(datetime, formatterDateTime)) getOrElse {
      datetime.split("""[ Tt]+""") match {
        case Array(date, time) =>
          toLocalDateTime(date, time)
        case Array(dOrT) =>
          if (containsDateKeys(dOrT)) toLocalDateTime(dOrT, "") else toLocalDateTime("", dOrT)
        case _ =>
          throw new IllegalArgumentException(s"$datetime is invalid iso datetime format")
      }
    }

  val DateKeys = List("年", "月")

  private def containsDateKeys(dOrT: String) = {
    DateKeys.exists(v => dOrT.contains(v))
  }

  def toLocalDateTime(instant: Instant): LocalDateTime =
    LocalDateTime.ofInstant(instant, ZONE_CHINA_OFFSET)

  def toLocalDateTime(epochMilli: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZONE_CHINA_OFFSET)

  def toLocalDateTime(date: Date): LocalDateTime = {
    if (date eq null) null
    else LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime), ZONE_CHINA_OFFSET)
  }

  def toDate(ldt: LocalDateTime): Date = Date.from(ldt.toInstant(ZONE_CHINA_OFFSET))

  def toDate(zdt: ZonedDateTime): Date = Date.from(zdt.toInstant)

  def toEpochMilli(dt: LocalDateTime): Long = dt.toInstant(ZONE_CHINA_OFFSET).toEpochMilli

  def toEpochMilli(dt: String): Long = toLocalDateTime(dt).toInstant(ZONE_CHINA_OFFSET).toEpochMilli

  def toSqlTimestamp(dt: LocalDateTime): SQLTimestamp = SQLTimestamp.valueOf(dt)

  def toSqlTimestamp(zdt: ZonedDateTime): SQLTimestamp = SQLTimestamp.from(zdt.toInstant)

  def toSqlDate(date: LocalDate) = new SQLDate(toEpochMilli(date.atStartOfDay()))

  def toSqlTime(time: LocalTime) = new SQLTime(toEpochMilli(time.atDate(LocalDate.now())))

  /**
   * @return 一天的开始：
   */
  def nowBegin(): LocalDateTime = LocalDate.now().atTime(0, 0, 0, 0)

  /**
   * @return 一天的结尾：
   */
  def nowEnd(): LocalDateTime = LocalTime.of(23, 59, 59, 999999999).atDate(LocalDate.now())

  def toDayInt(localDateTime: LocalDateTime): Int = {
    toDayInt(localDateTime.toLocalDate)
  }

  /**
   * 将 LocalDate(2017-11-21) 转换成 20171121 (Int类型)
   * @param localDate
   * @return
   */
  def toDayInt(localDate: LocalDate): Int = {
    localDate.getYear * 10000 + localDate.getMonthValue * 100 + localDate.getDayOfMonth
  }

  private[this] val funcId = new java.util.concurrent.atomic.AtomicInteger()

  def time[R](func: => R): R = {
    val fid = funcId.incrementAndGet()
    val start = Instant.now()
    logger.info(s"funcId: $fid start time: $start")
    try {
      func
    } finally {
      val end = Instant.now()
      val cost = Duration.between(start, end)
      logger.info(s"funcId: $fid end time: $end, cost: $cost")
    }
  }

}

object TimeUtils extends TimeUtils {

}
