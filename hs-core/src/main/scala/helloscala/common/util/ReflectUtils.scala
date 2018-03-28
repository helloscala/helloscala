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

package helloscala.common.util

import java.net.{URI, URL}

import java.nio.file.{DirectoryStream, Files, Path, Paths}
import java.util.zip.{ZipEntry, ZipFile}

import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.reflect.runtime.universe

case class Person(name: String, age: Int)

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-04-10.
 */
class ReflectUtils extends StrictLogging {

  lazy val mirror: universe.Mirror = universe.runtimeMirror(Thread.currentThread().getContextClassLoader)

  def getUniverseType(className: String): universe.Type = {
    val sym = mirror.staticClass(className)
    sym.selfType
  }

  /**
   * 根据包路径遍历获取所有 class 文件全限定名
   *
   * @param packagePath 包路径
   * @return 所有 class 文件全限定名
   */
  def listClassNameFromPackage(packagePath: String): List[String] = {
    listPathnameFromPackage(packagePath)
      .map(path => {
        val pathname = path.toString.replace('/', '.')
        val start = pathname.indexOf(packagePath)
        pathname.substring(start, pathname.length - 6)
      })
  }

  def listPathnameFromPackage(pkg: String): List[String] = {
    try {
      val dir = pkg.replace('.', '/')
      val url = Thread.currentThread().getContextClassLoader.getResource(dir)

      if (url.getProtocol == "jar") {
        val path = new URI(url.getPath).getPath
        listPathnameFromPackageByZip(pkg, dir, path)
      } else {
        listPathnameFromPackageByFile(pkg, dir, url)
      }
    } catch {
      case e: Exception =>
        logger.error(s"pkg: $pkg", e)
        Nil
    }
  }

  private def listPathnameFromPackageByFile(pkg: String, dir: String, url: URL) = {
    val directories = mutable.Queue.empty[Path]
    val filter = new DirectoryStream.Filter[Path] {
      override def accept(path: Path): Boolean = {
        if (Files.isDirectory(path)) {
          directories.enqueue(path)
          false
        } else {
          val name = path.getFileName.toString
          name.endsWith(".class") && !name.endsWith("$.class") && !name.contains("$$")
        }
      }
    }

    try {
      var paths = listPaths(Paths.get(url.toURI), filter).map(_.toString)

      while (directories.nonEmpty) {
        val path = directories.dequeue()
        paths = paths ::: listPaths(path, filter).map(_.toString)
      }

      paths
    } catch {
      case e: Exception =>
        logger.error(s"pkg: $pkg, dir: $dir, getProtocol: ${url.getProtocol}", e)
        Nil
    }
  }

  private def listPathnameFromPackageByZip(pkg: String, dir: String, path: String): List[String] = {
    def filter(entry: ZipEntry): Boolean = {
      val name = entry.getName
      !entry.isDirectory && name.startsWith(dir) && name.endsWith(".class") && !name.endsWith("$.class") && !name.contains("$$")
    }

    val bangIndex = path.indexOf('!')
    val filePath = path.substring(0, bangIndex)

    try {
      listPaths(new ZipFile(filePath), filter)
    } catch {
      case e: Exception =>
        logger.error(s"path: $path", e)
        Nil
    }
  }

  private def listPaths(zipFile: ZipFile, filter: ZipEntry => Boolean): List[String] = {
    var paths = List.empty[String]
    val entries = zipFile.entries()

    while (entries.hasMoreElements) {
      val jarEntry = entries.nextElement()
      if (filter(jarEntry)) {
        paths ::= jarEntry.getName
      }
    }
    paths
  }

  private def listPaths(path: Path, filter: DirectoryStream.Filter[Path]): List[Path] = {
    val ds = Files.newDirectoryStream(path, filter)
    try {
      ds.iterator().asScala.toList
    } finally {
      ds.close()
    }
  }
}

object ReflectUtils extends ReflectUtils
