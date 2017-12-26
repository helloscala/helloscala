package helloscala.common

import java.util.concurrent.{ Callable, CompletionStage }

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
