package helloscala.jdbc

object AndThanDemo extends App {

  val f1: String => String = a => a + "杨"
  val f2: String => String = a => a + "景"

  val f3 = f1 andThen f2
  println(f3("--"))

  val f4 = f1 compose f2
  println(f4("--"))

}
