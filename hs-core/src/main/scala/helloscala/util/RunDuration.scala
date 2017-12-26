package helloscala.util

import java.time.{ Duration, Instant }

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
