package helloscala.common.util

import java.nio.file.StandardOpenOption.{CREATE, TRUNCATE_EXISTING, WRITE}
import java.nio.file.{OpenOption, Path}

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink}
import akka.util.ByteString

import scala.concurrent.Future

object FileUtils {

  /**
   * 获取一个按行写入文件的Sink
   * @param filename 要写入的文件
   * @param options 文件打开选项
   * @param lineSeparator 行分隔符
   * @return
   */
  def lineSink(
      filename: Path,
      options: Set[OpenOption] = Set(WRITE, TRUNCATE_EXISTING, CREATE),
      lineSeparator: String = "\n"): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s + lineSeparator))
      .toMat(FileIO.toPath(filename, options))(Keep.right)

}
