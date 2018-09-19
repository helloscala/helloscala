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

import scala.util.control.NonFatal

object AsLong {

  def unapply(str: String): Option[Long] =
    try {
      Some(str.toLong)
    } catch {
      case NonFatal(e) => None
    }
}

object AsInt {

  def unapply(str: String): Option[Int] =
    try {
      Some(str.toInt)
    } catch {
      case NonFatal(e) => None
    }
}
