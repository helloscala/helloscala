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

package helloscala.http.session

import java.net.{URLDecoder, URLEncoder}

import scala.util.Try

trait SessionSerializer[T, R] {
  def serialize(t: T): R
  def deserialize(r: R): Try[T]
}

class SingleValueSessionSerializer[T, V](toValue: T => V, fromValue: V => Try[T])(
    implicit
    valueSerializer: SessionSerializer[V, String]) extends SessionSerializer[T, String] {

  override def serialize(t: T) = valueSerializer.serialize(toValue(t))

  override def deserialize(r: String) = valueSerializer.deserialize(r).flatMap(fromValue)
}

class MultiValueSessionSerializer[T](toMap: T => Map[String, String], fromMap: Map[String, String] => Try[T])
  extends SessionSerializer[T, String] {

  import SessionSerializer._

  override def serialize(t: T) = toMap(t)
    .map { case (k, v) => urlEncode(k) + "~" + urlEncode(v) }
    .mkString("&")

  override def deserialize(s: String) = {
    Try {
      if (s == "") Map.empty[String, String]
      else {
        s
          .split("&")
          .map(_.split("~", 2))
          .map(p => urlDecode(p(0)) -> urlDecode(p(1)))
          .toMap
      }
    }.flatMap(fromMap)
  }
}

object SessionSerializer {
  implicit def stringToStringSessionSerializer: SessionSerializer[String, String] = new SessionSerializer[String, String] {
    override def serialize(t: String) = urlEncode(t)
    override def deserialize(s: String) = Try(urlDecode(s))
  }

  implicit def intToStringSessionSerializer: SessionSerializer[Int, String] = new SessionSerializer[Int, String] {
    override def serialize(t: Int) = urlEncode(t.toString)
    override def deserialize(s: String) = Try(urlDecode(s).toInt)
  }

  implicit def longToStringSessionSerializer: SessionSerializer[Long, String] = new SessionSerializer[Long, String] {
    override def serialize(t: Long) = urlEncode(t.toString)
    override def deserialize(s: String) = Try(urlDecode(s).toLong)
  }

  implicit def floatToStringSessionSerializer: SessionSerializer[Float, String] = new SessionSerializer[Float, String] {
    override def serialize(t: Float) = urlEncode(t.toString)
    override def deserialize(s: String) = Try(urlDecode(s).toFloat)
  }

  implicit def doubleToStringSessionSerializer: SessionSerializer[Double, String] = new SessionSerializer[Double, String] {
    override def serialize(t: Double) = urlEncode(t.toString)
    override def deserialize(s: String) = Try(urlDecode(s).toDouble)
  }

  implicit def mapToStringSessionSerializer: SessionSerializer[Map[String, String], String] =
    new MultiValueSessionSerializer[Map[String, String]](identity, Try(_))

  private[session] def urlEncode(s: String): String = URLEncoder.encode(s, "UTF-8")
  private[session] def urlDecode(s: String): String = URLDecoder.decode(s, "UTF-8")
}
