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

import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable

object Graphs extends StrictLogging {

  /**
   * 广度优先搜索（非加权图中查找最短路径）
   * @param graph 图（有向图）
   * @param start 起始节点
   * @param comparison 比较函数
   * @return 广度搜索 graph，每个节点调用 comparison 函数。comparison 函数节点为true时返回 Some(node)，否则返回 None
   */
  def breadthFirstSearch[T](
      graph: Map[T, Seq[T]],
      start: T,
      comparison: T => Boolean
  ): Option[T] = {
    val firstSearchs = graph.getOrElse(start, Vector.empty)
    val searchQueue = mutable.Queue[T](firstSearchs: _*)
    val searched = mutable.Set[T](firstSearchs: _*)

    var count = 0
    while (searchQueue.nonEmpty) {
      count += 1
      val person = searchQueue.dequeue()
      if (comparison(person)) {
        logger.debug(s"[$count] 找到数据 $person $searchQueue")
        return Some(person)
      } else {
        // 已检测过的节点不再加入搜索队列
        val pendings = graph
          .getOrElse(person, Vector.empty)
          .flatMap(pending =>
            if (searched(pending)) {
              None
            } else {
              searched += pending
              Some(pending)
          })

        searchQueue.enqueue(pendings: _*)
        logger.debug(s"[$count] $person $searchQueue")
      }
    }

    None
  }

  /**
   * 狄克斯特拉查找算法（加权图中查找最短路径）
   * @param graph 加权图
   * @param start 起始节点
   * @param end 终点节点
   * @return (最短路径的加权总值，最短路径)
   */
  def dijkstraSearch[T](
      graph: Map[T, Map[T, Int]],
      start: T,
      end: T
  ): (Int, List[T]) = {
    val processed = mutable.Set.empty[T]

    val startNeighbors = graph(start)

    // start 节点到任一节点最短花费
    val costs = mutable.Map.empty[T, Int] ++
      startNeighbors ++
      graph.keysIterator
        .filterNot(node => node == start || startNeighbors.contains(node))
        .map(node => node -> Int.MaxValue)

    // 最短花费的父节点映射
    val parents = mutable.Map.empty[T, Option[T]] ++ graph(start).keys
      .map(key => (key, Some(start)))

    /**
     * 从未处理节点中找到开销最小的节点
     * @param costs 未处理节点
     * @return 找到返回Some[T]，否则返回None
     */
    def findLowestCostNode(costs: scala.collection.Map[T, Int]): Option[T] = {
      var lowestCost = Int.MaxValue
      var lowestCostNode = Option.empty[T]
      for ((node, cost) <- costs if cost < lowestCost && !processed(node)) {
        lowestCost = cost
        lowestCostNode = Some(node)
      }
      lowestCostNode
    }

    var count = 0
    var maybeNode = findLowestCostNode(costs)
    while (maybeNode.nonEmpty) {
      val node = maybeNode.get
      val cost = costs(node)
      val neighbors = graph(node)
      for (n <- neighbors.keysIterator) {
        val newCost = cost + neighbors(n)
        if (costs(n) > newCost) {
          costs(n) = newCost
          parents(n) = Some(node)
        }
      }
      processed += node
      maybeNode = findLowestCostNode(costs)
    }

    logger.debug(s"costs: $costs, parents: $parents")

    var paths = List(end)
    var path = parents.get(end).flatten
    while (path.nonEmpty) {
      paths ::= path.get
      path = parents.get(path.get).flatten
    }
    (costs(end), paths)
  }

}
