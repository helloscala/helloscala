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

import java.time.{Duration, Instant}

final class RunDuration private () {
  private[this] var _start: Instant = _
  private[this] var _end: Instant = _
  private[this] var _duration: Duration = _

  def run(): Instant = {
    _start = Instant.now()
    _start
  }

  def complete(): Duration = {
    _end = Instant.now()
    _duration = Duration.between(_start, _end)
    _duration
  }

  def duration: Duration = _duration

  def start: Instant = _start

  def end: Instant = _end

}

object RunDuration {

  def runNew(): RunDuration = {
    val rd = new RunDuration()
    rd.run()
    rd
  }

}
