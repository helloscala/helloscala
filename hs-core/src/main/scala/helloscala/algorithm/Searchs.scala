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

import scala.annotation.tailrec

object Searchs {
  /**
   * 泛型二分查找
   * @param list 待查找的列表
   * @param item 要查找的值
   * @param ev1 隐式函数（将元素转变成可比较的）
   * @return
   */
  def genericBinarySearch[T](list: IndexedSeq[T], item: T)(implicit ev1: T => Ordered[T]): Int = {
    @tailrec
    def _search(low: Int, high: Int): Int = {
      val mid = (low + high) / 2
      if (mid > high) {
        -1
      } else {
        val guess = list(mid)
        //        println(s"low: $low, high: $high, mid: $mid, guess: $guess ? item: $item")
        if (guess == item) mid
        else if (guess > item) _search(low, mid - 1)
        else if (guess < item) _search(mid + 1, high)
        else -1
      }
    }

    if (list.isEmpty) -1
    else _search(0, list.size - 1)
  }

}
