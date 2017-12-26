package helloscala

abstract class Logger {
  def log(msg: String) {}
}

trait ALogger extends Logger {
  abstract override def log(msg: String): Unit = super.log(msg + " ALogger")
}

trait BLogger extends Logger {
  abstract override def log(msg: String): Unit = super.log(msg + " BLogger ")
}

class XLogger extends Logger /*with ALogger with BLogger */ {
  override def log(msg: String): Unit = {
    println(msg)
  }
}

object AbstractTrait {

  def splitTail[A](iter: Traversable[A]) = {
    iter.tail
  }

  def main(args: Array[String]): Unit = {
    val tail = splitTail(Array(1, 2, 3))
    println(tail)
  }

}
