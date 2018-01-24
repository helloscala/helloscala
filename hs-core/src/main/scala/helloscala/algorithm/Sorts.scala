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
