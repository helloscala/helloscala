package helloscala.http.model

import akka.http.scaladsl.model.headers.RawHeader

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-05-03.
 */
object HSServerHeader {
  val name: String = "HS-Server"

  def apply(value: String) = RawHeader(name, value)
}
