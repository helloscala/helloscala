package helloscala.inject.component

import java.util.concurrent.ConcurrentLinkedDeque
import javax.inject.{ Inject, Singleton }

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
