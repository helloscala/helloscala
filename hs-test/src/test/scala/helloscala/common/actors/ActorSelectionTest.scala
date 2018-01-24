package helloscala.common.actors

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import helloscala.common.actor.MetricActor
import helloscala.test.HelloscalaSpec
import org.scalatest.BeforeAndAfterAll

class ActorC extends MetricActor {
  override def receive = {
    case v => logger.debug(s"Receive message: $v")
  }
}

class ActorB extends MetricActor {
  context.actorOf(Props[ActorC], "c")

  override def receive = {
    case v => logger.debug(s"Receive message: $v")
  }
}

class ActorA extends MetricActor {
  context.actorOf(Props[ActorB], "b")

  override def receive = {
    case v => logger.debug(s"Receive message: $v")
  }
}

class ActorSelectionTest extends HelloscalaSpec with BeforeAndAfterAll {

  val system = ActorSystem()

  "actor" should {
    "selection" in {
      val a = system.actorOf(Props[ActorA], "a")

      val selectC = system.actorSelection("/user/a/b/c")
      println(s"selectC: $selectC")
      selectC ! "哈哈哈"
    }
  }

  override protected def afterAll(): Unit = {
    TimeUnit.SECONDS.sleep(3)
    val result = system.terminate().futureValue
    logger.info(s"ActorSystem: $system exists status: $result")
  }

}
