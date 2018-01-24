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
