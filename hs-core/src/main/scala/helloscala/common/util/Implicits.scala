package helloscala.common.util

import helloscala.common.util.Implicits.Option2Either

import scala.language.implicitConversions

trait Implicits {
  implicit def option2Either[R](maybe: Option[R]): Option2Either[R] = new Option2Either(maybe)
}

object Implicits {

  implicit class Option2Either[R](maybe: Option[R]) {

    /**
     * 当Option为None时，将其转换成Either[L, R]，并使用left设置L值；Option为Some时，设置Either.right(...)
     * @param left 可能将被用于设置的Either.left值
     * @tparam L
     * @return Either
     */
    def toEither[L](left: => L): Either[L, R] = {
      maybe match {
        case Some(value) => Right(value)
        case None        => Left(left)
      }
    }
  }

}
