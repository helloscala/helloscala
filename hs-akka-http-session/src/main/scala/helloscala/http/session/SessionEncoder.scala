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

import helloscala.common.aes.{Crypto, SessionUtil}

import scala.util.Try

trait SessionEncoder[T] {
  def encode(t: T, nowMillis: Long, config: SessionConfig): String

  def decode(s: String, config: SessionConfig): Try[DecodeResult[T]]
}

object SessionEncoder {

  /**
   * Default low-priority implicit encoder. If you wish to use another one, provide an implicit encoder in a
   * higher-priority implicit scope, e.g. as an implicit value declared next to `SessionManager`.
   */
  implicit def basic[T](implicit serializer: SessionSerializer[T, String]) =
    new BasicSessionEncoder[T]()
}

case class DecodeResult[T](t: T, expires: Option[Long], signatureMatches: Boolean)

/**
 * @param serializer Must create cookie-safe strings (only with allowed characters).
 */
class BasicSessionEncoder[T](implicit serializer: SessionSerializer[T, String]) extends SessionEncoder[T] {

  override def encode(t: T, nowMillis: Long, config: SessionConfig) = {
    // adding an "x" so that the string is never empty, even if there's no data
    val serialized = "x" + serializer.serialize(t)

    val withExpiry = config.sessionMaxAgeSeconds.fold(serialized) { maxAge =>
      val expiry = nowMillis + maxAge * 1000L
      s"$expiry-$serialized"
    }

    val encrypted =
      if (config.sessionEncryptData)
        Crypto.encryptAES(withExpiry, config.serverSecret)
      else withExpiry

    s"${Crypto.signHmacSHA1Hex(withExpiry, config.serverSecret)}-$encrypted"
  }

  override def decode(s: String, config: SessionConfig): Try[DecodeResult[T]] = {
    def extractExpiry(data: String): (Option[Long], String) = {
      config.sessionMaxAgeSeconds.fold((Option.empty[Long], data)) { maxAge =>
        val splitted = data.split("-", 2)
        (Some(splitted(0).toLong), splitted(1))
      }
    }

    Try {
      val splitted = s.split("-", 2)
      val decrypted =
        if (config.sessionEncryptData)
          Crypto.decryptAES(splitted(1), config.serverSecret)
        else splitted(1)

      val (expiry, serialized) = extractExpiry(decrypted)

      val signatureMatches =
        SessionUtil.constantTimeEquals(splitted(0), Crypto.signHmacSHA1Hex(decrypted, config.serverSecret))

      serializer.deserialize(serialized.substring(1)).map {
        DecodeResult(_, expiry, signatureMatches)
      }
    }.flatten
  }
}
