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
