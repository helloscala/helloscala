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

package helloscala.common

import java.util.concurrent.{Callable, CompletionStage}

import scala.compat.java8.FutureConverters
import scala.concurrent.Future

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-02-21.
 */
trait AppLifecycle {
  def addStopHook(hook: () => Future[_]): Unit

  def addStopHook(hook: Callable[_ <: CompletionStage[_]]): Unit = {
    addStopHook(() => FutureConverters.toScala(hook.call().asInstanceOf[CompletionStage[_]]))
  }
}
