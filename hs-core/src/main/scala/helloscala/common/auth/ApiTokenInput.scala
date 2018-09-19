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

package helloscala.common.auth

import java.time.ZonedDateTime

import helloscala.common.types.ObjectId
import helloscala.common.util.StringUtils

import scala.collection.immutable

trait BaseApiToken {
  def id: String
  def key: String
  def encodingAesKey: String
  def email: String
  def domain: Option[String]
  def createdAt: ZonedDateTime
}

/**
 * accessToken = sha256Hex(appId + timestamp + echoStr + token)
 *
 * @param appId       App Account Id
 * @param timestamp   调用方传过来的时间戳，单位：秒
 * @param echoStr     随机字符串（不能超过40位字符，有效字符范围：[a-z][A-Z][0-9]）
 * @param accessToken 调用方传过来的访问令牌
 */
case class ApiTokenInput(appId: String, timestamp: String, echoStr: String, accessToken: String) {

  import ApiTokenInput._

  require(StringUtils.isNoneBlank(timestamp), "hl-timestamp 不能为空")
  require(StringUtils.isNoneBlank(echoStr), "echo-str 不能为空")
  require(StringUtils.isNoneBlank(accessToken), "hl-access-token 不能为空")
  require(echoStr.length <= 40 && echoStr.forall(c => VALID_CHARS.contains(c)),
          "随机字符串（不能超过40位字符，有效字符范围：[a-z][A-Z][0-9]）")
}

object ApiTokenInput {

  val VALID_CHARS: immutable.IndexedSeq[Char] = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  val validChars: Set[Char] = VALID_CHARS.toSet

}
