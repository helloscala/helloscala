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

package helloscala.edoc

import java.io.{IOException, InputStream}
import java.nio.file.{Files, Path}

import helloscala.common.util.StringUtils
import org.apache.poi.POIXMLDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.tika.Tika
import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.BodyContentHandler

object DocumentUtils extends DocumentUtils {

}

class DocumentUtils {
  val config = TikaConfig.getDefaultConfig
  val tika = new Tika(config)

  def splitNameSuffix(str: String) = {
    if (StringUtils.isBlank(str)) {
      (str, "")
    } else {
      val lastIdx = str.lastIndexOf('.')
      if (lastIdx < 0) {
        (str, "")
      } else {
        (str.substring(0, lastIdx), str.substring(lastIdx + 1, str.length))
      }
    }
  }

  /**
   * 注意：此方法调用完成后将自动关闭 in
   *
   * @param in 输入流
   * @return
   */
  def getMediaType(in: InputStream): MediaType = {
    val tin = TikaInputStream.get(in)
    try {
      val metadata = new Metadata
      val mediaType = tika.getDetector.detect(tin, metadata)
      //      val mimeType = config.getMimeRepository.forName(mediaType.toString)
      //      config.getMimeRepository
      mediaType
    } finally {
      if (tin != null) tin.close()
    }
  }

  def isDocx(path: Path): Boolean = path.getFileName.toString.endsWith(".docx")

  def isDoc(path: Path): Boolean = path.getFileName.toString.endsWith(".doc")

  def isMSDoc(path: Path): Boolean = isDocx(path) || isDoc(path)

  @throws[IOException]
  def getTextFromPath(path: Path): String = {
    val fileName = path.getFileName.toString
    val isDocx = fileName.endsWith(".docx")
    val extractor =
      if (isDocx) new XWPFWordExtractor(POIXMLDocument.openPackage(path.toString))
      else new WordExtractor(Files.newInputStream(path))
    try {
      val text = extractor.getText
      extractor.close()
      text
    } finally {
      extractor.close()
    }
  }

  def getDocumentFromPath(path: Path): (String, Metadata) = {
    val in = Files.newInputStream(path)
    try {
      val (handler, metadata) = getDocumentFromInputStream(in)
      handler.toString -> metadata
    } finally {
      if (in != null) in.close()
    }
  }

  def getDocumentFromInputStream(in: InputStream): (BodyContentHandler, Metadata) = {
    val handler = new BodyContentHandler(-1)
    val parser = new AutoDetectParser
    val metadata = new Metadata
    parser.parse(in, handler, metadata)
    handler -> metadata
  }

  def toMap(metadata: Metadata): Map[String, String] =
    metadata.names().map(name => name -> metadata.getValues(name).mkString("; ")).toMap

}
