package helloscala.util

import java.time.LocalDateTime

import org.scalatest.{ MustMatchers, WordSpec }

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-20.
 */
class TimeUtilsTest extends WordSpec with MustMatchers {

  "TimeUtilsTest" must {
    "toLocalDate" in {
      val strLdt = "2017-01-23  22:32:11"
      TimeUtils.toLocalDateTime(strLdt) mustBe LocalDateTime.of(2017, 1, 23, 22, 32, 11)
    }
  }

}
