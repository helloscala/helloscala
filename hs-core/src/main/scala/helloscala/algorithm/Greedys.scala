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

object Greedys {

  /**
   * 贪婪算法（近似算法） http://www.ituring.com.cn/book/1864 8.3节
   * @param stations 广播站与覆盖省份映射
   * @param states 需要
   * @tparam T
   * @return
   */
  def greedys[T](stations: Map[T, Set[T]], states: Set[T]): Set[T] = {
    val finalStations = mutable.Set.empty[T]
    var statesNeeded = Set.empty[T] ++ states

    while (statesNeeded.nonEmpty) {
      var bestStation = Option.empty[T]
      var statesCovered = Set.empty[T]
      for ((station, statesForStation) <- stations) {
        val covered = statesNeeded.intersect(statesForStation)
        if (covered.size > statesCovered.size) {
          bestStation = Some(station)
          statesCovered = covered
        }
      }

      statesNeeded --= statesCovered
      bestStation.foreach(station => finalStations += station)
    }

    finalStations.toSet
  }

}
