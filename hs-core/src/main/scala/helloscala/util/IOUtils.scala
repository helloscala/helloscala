/**
 * 文件/IO 相关帮助函数
 * Created by yangbajing(yangbajing@gmail.com) on 2017-05-12.
 */
package helloscala.util

import java.io.{ IOException, InputStream }
import java.nio.charset.{ Charset, StandardCharsets }
import java.nio.file.{ CopyOption, Files, Path }
import java.util.zip.ZipInputStream

import akka.stream.Materializer
import akka.stream.scaladsl.{ Source, StreamConverters }
import akka.util.ByteString

import scala.annotation.varargs
import scala.collection.mutable
import scala.concurrent.duration.{ FiniteDuration, _ }

class IOUtils {

  @varargs
  def move(source: Path, target: Path, options: CopyOption*): Path = {
    if (!Files.isDirectory(target.getParent)) {
      Files.createDirectories(target.getParent)
    }
    Files.move(source, target, options: _*)
  }

  def readLine(
    bytes: Source[ByteString, Any],
    charset: Charset = StandardCharsets.UTF_8)(implicit materializer: Materializer, readTimeout: FiniteDuration = 5.seconds): Vector[String] = {
    val in = bytes.runWith(StreamConverters.asInputStream(readTimeout))
    try {
      scala.io.Source.fromInputStream(in, charset.name()).getLines().toVector
    } finally {
      if (in != null) in.close()
    }
  }

  def readAllContentsFromZipBySource(
    bytes: Source[ByteString, Any],
    charset: Charset = StandardCharsets.UTF_8)(implicit materializer: Materializer, readTimeout: FiniteDuration = 5.seconds): Vector[(String, String)] = {
    val in = bytes.runWith(StreamConverters.asInputStream(readTimeout))
    readAllContentsFromZip(in, charset)
  }

  /**
   * 从ZIP压缩包读取所有文件内容
   *
   * @param in      InputStream
   * @param charset 字符集，默认UTF-8
   * @return 成功返回ZIP压缩包内 (文件名, 内容) 的列表，失败抛出运行时异常
   */
  @throws[IOException]
  def readAllContentsFromZip(in: InputStream, charset: Charset = StandardCharsets.UTF_8): Vector[(String, String)] = {
    val zip = new ZipInputStream(in, charset)
    try {
      val contents = mutable.ArrayBuffer.empty[(String, String)]

      var entry = zip.getNextEntry
      while (entry != null) {
        if (!entry.isDirectory) {
          val buf = new Array[Byte](entry.getSize.toInt)
          zip.read(buf)
          val content = new String(buf, StandardCharsets.UTF_8)
          contents.append(entry.getName -> content)
        }

        entry = zip.getNextEntry
      }

      contents.toVector
    } finally {
      if (zip != null) zip.close()
    }

  }

  def readLineFromResources(path: String): List[String] = {
    val in = Thread.currentThread().getContextClassLoader.getResourceAsStream(path)
    require(in != null, s"resources资源：$path 不存在")
    val s = scala.io.Source.fromInputStream(in)
    try {
      s.getLines().toList
    } finally {
      s.close()
    }
  }

}

object IOUtils extends IOUtils {

}
