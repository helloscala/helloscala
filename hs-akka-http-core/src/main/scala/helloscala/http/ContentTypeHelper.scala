package helloscala.http

import akka.http.scaladsl.model.{ ContentType, MediaTypes }

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
