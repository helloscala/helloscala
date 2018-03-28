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

package helloscala.inject.component.http

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import helloscala.common.exception.HSException
import helloscala.common.{Configuration, ErrCodes}
import helloscala.http.HttpConstants
import helloscala.inject.component.DefaultAppLifecycle

import scala.concurrent.Await
import scala.concurrent.duration._

@Singleton
class CommonRoutes @Inject() (
    appLifecycle: DefaultAppLifecycle,
    actorSystem: ActorSystem,
    configuration: Configuration) {

  def htmlRoute: Route = {
    if (configuration.has(HttpConstants.CONFIG_PATH_PREFIX)) {
      configuration.get[Option[String]](s"${HttpConstants.CONFIG_PATH_PREFIX}.html-resource-path") match {
        case Some(path) =>
          getFromResourceDirectory(path) ~ getFromResource(s"$path/index.html")
        case _ =>
          val path = configuration.getString(s"${HttpConstants.CONFIG_PATH_PREFIX}.html-path")
          getFromDirectory(path) ~ getFromFile(s"$path/index.html")
      }
    } else {
      reject
    }
  }

  def shutdown(): Unit = {
    try {
      Await.result(appLifecycle.stop(), 60.seconds)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

    println("Begin shutdown the system!")

    try {
      Await.result(actorSystem.terminate(), 60.seconds)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

    println("Shutting down System!")
  }

  def shutdownRoute: Route =
    path("shutdown") {
      post { ctx =>
        val inets = ctx.request.uri.authority.host.inetAddresses
        if (!inets.exists(inet => inet.isAnyLocalAddress)) {
          throw new HSException(ErrCodes.UNKNOWN, "关闭系统指令只允许通过 0.0.0.0 地址发送！")
        }
        shutdownThread.start()
        ctx.complete("收到系统关闭命令，开始执行……\n")
      }
    }

  def healthCheckRoute: Route = path("health_check") {
    get {
      complete(HttpEntity.Empty)
    }
  }

  private def shutdownThread = new Thread() {
    override def run(): Unit = {
      shutdown()
    }
  }

}
