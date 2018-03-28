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

package helloscala.inject.component

import java.util.concurrent.ConcurrentLinkedDeque
import javax.inject.{Inject, Singleton}

import helloscala.common.AppLifecycle
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.concurrent.Future

/**
 * Default implementation of the application lifecycle.
 */
@Singleton
class DefaultAppLifecycle @Inject() () extends AppLifecycle {
  private val logger = LoggerFactory.getLogger(classOf[DefaultAppLifecycle])

  private val hooks = new ConcurrentLinkedDeque[() => Future[_]]()

  def addStopHook(hook: () => Future[_]): Unit = hooks.push(hook)

  /**
   * Call to shutdown the application.
   *
   * @return A future that will be redeemed once all hooks have executed.
   */
  def stop(): Future[_] = {

    // Do we care if one hook executes on another hooks redeeming thread? Hopefully not.
    import scala.concurrent.ExecutionContext.Implicits.global

    @tailrec
    def clearHooks(previous: Future[Any] = Future.successful[Any](())): Future[Any] = {
      val hook = hooks.poll()
      if (hook != null) {
        clearHooks(previous.flatMap { _ =>
          hook().recover {
            case e => logger.error("Error executing stop hook", e)
          }
        })
      } else {
        previous
      }
    }

    clearHooks()
  }

}
