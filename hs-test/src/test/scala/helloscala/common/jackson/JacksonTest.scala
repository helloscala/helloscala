package helloscala.common.jackson

import java.time.{LocalDate, LocalDateTime, LocalTime, ZonedDateTime}

import helloscala.test.HelloscalaSpec

class Java8Time {
  var ldt: LocalDateTime = _
  var zdt: ZonedDateTime = _
  var ld: LocalDate = _
  var lt: LocalTime = _

  override def toString = s"Java8Time($ldt, $zdt, $ld, $lt)"
}

class JacksonTest extends HelloscalaSpec {

  "java8 time" in {
    val jt = new Java8Time
    jt.ldt = LocalDateTime.now()
    jt.zdt = ZonedDateTime.now()
    jt.ld = LocalDate.now()
    jt.lt = LocalTime.now()

    val str = Jackson.defaultObjectMapper.writeValueAsString(jt)
    println(s"str: $str")

    val jt2 = Jackson.defaultObjectMapper.readValue(str, classOf[Java8Time])
    println(jt2)
  }

}
