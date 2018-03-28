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

package helloscala.algorithm

import scala.collection.mutable

object Sorts {

  def genericQuicksort[T](list: IndexedSeq[T])(implicit ev1: T => Ordered[T]): Vector[T] = {
    if (list.size < 2) {
      list.toVector
    } else {
      val len = list.size
      val pivot = list(len / 2)
      val less = mutable.ArrayBuffer.empty[T]
      val greater = mutable.ArrayBuffer.empty[T]
      val pivots = mutable.ArrayBuffer.empty[T]
      for (item <- list) {
        if (item < pivot) less.append(item)
        else if (item > pivot) greater.append(item)
        else pivots.append(item)
      }
      genericQuicksort(less) ++ pivots ++ genericQuicksort(greater)
    }
  }

}
