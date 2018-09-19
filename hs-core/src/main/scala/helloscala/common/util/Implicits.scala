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

package helloscala.common.util

import helloscala.common.util.Implicits.Option2Either

import scala.language.implicitConversions

trait Implicits {
  implicit def option2Either[R](maybe: Option[R]): Option2Either[R] =
    new Option2Either(maybe)
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
