package helloscala.inject

import akka.http.scaladsl.server.{ ExceptionHandler, RejectionHandler }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import helloscala.http.core.server.{ BaseExceptionPF, BaseRejectionBuilder }
import helloscala.test.HelloscalaSpec
import org.scalatest.BeforeAndAfterAll

trait AkkaHttpSpec
  extends HelloscalaSpec
  with BeforeAndAfterAll
  with InjectSystemSupport
  with BaseExceptionPF
  with BaseRejectionBuilder
  with ScalatestRouteTest {

  implicit protected def _rejectionHandler: RejectionHandler = rejectionHandler

  implicit protected def _exceptionHandler: ExceptionHandler = exceptionHandler

}
