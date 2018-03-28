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

package helloscala.common.actor

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-21.
 */
trait MetricActor extends Actor with LazyLogging {
  override def preStart(): Unit = {
    super.preStart()
    logger.debug(s"[preStart] $self")
  }

  override def postStop(): Unit = {
    logger.debug(s"[postStop] $self")
    super.postStop()
  }

  override def receive: Receive = {
    case unknown =>
      logger.warn(s"未知消息：$unknown")
  }

  protected def stop(): Unit = {
    context.stop(self)
  }
}

object MetricActor {
  def name(name: String) = s"$name-$nameInc"

  def nameInc: Int = counter.getAndIncrement()

  private val counter = new AtomicInteger()
}
