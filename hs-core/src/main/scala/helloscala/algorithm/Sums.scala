package helloscala.algorithm

object Sums {

  /**
   * 递归求和
   * @param list 可迭代列表
   * @return
   */
  def recursiveSum(list: Iterable[Int]): Int = {
    if (list.isEmpty) 0
    else list.head + recursiveSum(list.tail)
  }

  def main(args: Array[String]): Unit = {

  }

}
