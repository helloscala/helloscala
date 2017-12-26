package helloscala.common.page

import java.time.{ LocalDate, LocalDateTime, LocalTime }
import java.util.UUID

import org.apache.commons.lang3.StringUtils

import scala.beans.BeanProperty
import scala.util.Try

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-28.
 */
trait PageSort {

}

object Page {
  /**
   * 默认页码
   */
  val DEFAULT_PAGE = 1

  /**
   * 默认返回记录条数
   */
  val DEFAULT_SIZE = 10
}

trait Page {
  def page: Int

  def size: Int

  def offset: Int = if (page < 2) 0 else (page - 1) * size
}

trait Pager[T] {
  def lastId: Option[T]

  def size: Int

  def params: Map[String, Any]
}

case class PageInput(
  @BeanProperty page: Int,
  @BeanProperty size: Int,
  @BeanProperty params: Map[String, Any] = Map()) extends Page {

  def toParamSeq: List[(String, Any)] = ("page", page) :: ("size", size) :: params.toList

  def toParamStringSeq: List[(String, String)] = toParamSeq.map { case (name, value) => (name, value.toString) }

  def getUUID(key: String): Option[UUID] = getString(key).flatMap(str => Try(UUID.fromString(str)).toOption)

  def getLocalDate(key: String): Option[LocalDate] =
    getString(key).flatMap(str => Try(LocalDate.parse(str)).toOption)

  def getLocalTime(key: String): Option[LocalTime] =
    getString(key).flatMap(str => Try(LocalTime.parse(str)).toOption)

  def getLocalDateTime(key: String): Option[LocalDateTime] =
    getString(key).flatMap(str => Try(LocalDateTime.parse(str)).toOption)

  def getDouble(key: String): Option[Double] =
    params.get(key)
      .flatMap {
        case d: Double => Some(d)
        case s: String => Try(s.toDouble).toOption
        case v => None
      }

  def getBoolean(key: String): Option[Boolean] =
    params.get(key)
      .flatMap {
        case b: Boolean => Some(b)
        case s: String => Try(s.toBoolean).toOption
        case _ => None
      }

  def getInt(key: String): Option[Int] =
    params.get(key)
      .flatMap {
        case i: Int => Some(i)
        case l: Long => Some(l.toInt)
        case s: String => Try(s.toInt).toOption
        case _ => None
      }

  def getLong(key: String): Option[Long] =
    params.get(key)
      .flatMap {
        case l: Long => Some(l)
        case i: Int => Some(i.toLong)
        case s: String => Try(s.toLong).toOption
        case _ => None
      }

  def getString(key: String): Option[String] =
    params.get(key)
      .flatMap {
        case s: String if StringUtils.isNoneBlank(s) => Some(s)
        case _ => None
      }

}

object PageInput {

  def applyBasic(page: Int, size: Int): PageInput = PageInput(page, size)

}

trait PageResult[T] extends Page {
  def content: Seq[T]

  def totalElements: Long

  def totalPages: Int = ((totalElements + size) / size).toInt
}

object PageResult {

  def apply[T](
    page: Int,
    size: Int,
    content: Seq[T],
    totalElements: Int): PageResult[T] =
    DefaultPageResult(page, size, content, totalElements)

}

case class DefaultPageResult[T](
  @BeanProperty page: Int,
  @BeanProperty size: Int,
  @BeanProperty content: Seq[T],
  @BeanProperty totalElements: Long) extends PageResult[T] {

  override def toString = s"DefaultPageResult($page, $size, $content, $totalElements, $totalPages)"
}