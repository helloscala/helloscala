package helloscala.common.util

import scala.util.control.NonFatal

object AsLong {
  def unapply(str: String): Option[Long] = try {
    Some(str.toLong)
  } catch {
    case NonFatal(e) => None
  }
}

object AsInt {
  def unapply(str: String): Option[Int] = try {
    Some(str.toInt)
  } catch {
    case NonFatal(e) => None
  }
}
