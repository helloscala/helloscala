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

package helloscala.http

import akka.http.scaladsl.model.{ContentType, MediaTypes}

trait ContentTypeHelper {

  def isDocx(ct: ContentType): Boolean = ct.mediaType == MediaTypes.`application/vnd.openxmlformats-officedocument.wordprocessingml.document`

  def isXlsx(ct: ContentType): Boolean = ct.mediaType == MediaTypes.`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

  def isPptx(ct: ContentType): Boolean = ct.mediaType == MediaTypes.`application/vnd.openxmlformats-officedocument.presentationml.presentation`

  def isDoc(ct: ContentType): Boolean = ct.mediaType == MediaTypes.`application/msword`

  def isXls(ct: ContentType): Boolean = ct.mediaType == MediaTypes.`application/excel`

  def isPpt(ct: ContentType): Boolean = ct.mediaType == MediaTypes.`application/mspowerpoint`

  def isPdf(ct: ContentType): Boolean = ct.mediaType == MediaTypes.`application/pdf`

  def isPlanText(ct: ContentType): Boolean = ct.mediaType == MediaTypes.`text/plain`
}
