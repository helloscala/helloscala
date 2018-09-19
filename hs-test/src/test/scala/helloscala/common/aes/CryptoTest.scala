package helloscala.common.aes

import java.time.Instant
import java.util.concurrent.TimeUnit

import helloscala.common.util.Utils
import helloscala.test.HelloscalaSpec

class CryptoTest extends HelloscalaSpec {
  val SPLIT: String = 0x1.toChar.toString

  def makeTest(due: Instant) =
    Seq(
      Utils.randomString(3),
      "passportId",
      Utils.randomString(3),
      "appId",
      Utils.randomString(3),
      "userId",
      Utils.randomString(3),
      due.getEpochSecond,
      Utils.randomString(3)
    ).mkString(SPLIT)

  "CryptoTest" should {
    //    val secret = "c05ll3lesrinf39t7mc5h6un6r0c69lgfno69dsak3vabeqamouq4328cuaekros401ajdpkh60rrtpd8ro24rbuqmgtnd1ebag6ljnb65i8a55d482ok7o0nch0bfbe"
    val secret = "89Ix0aWRhVWlHB5U4z1mPhNeNXIy4W8YNvhOu16pw2s"
    val due = Instant.now().plusSeconds(TimeUnit.DAYS.toSeconds(1))
    val text = makeTest(due)
    var encryptMsg: String = null

    "ddd" in {
      val s1 = Crypto.encryptAES(makeTest(due), secret)
      val s2 = Crypto.encryptAES(makeTest(due), secret)
      val s3 = Crypto.encryptAES(makeTest(due), secret)

      val t1 = Crypto.decryptAES(s1, secret)
      val t2 = Crypto.decryptAES(s2, secret)
      val t3 = Crypto.decryptAES(s3, secret)

      println(s"$s1\n$t1")
      println(s"$s2\n$t2")
      println(s"$s3\n$t3")
    }

    "encrypt" in {
      encryptMsg = Crypto.encryptAES(text, secret)
      println(s"Encrypt Msg: $encryptMsg")
    }

    "decrypt" in {
      val decryptMsg = Crypto.decryptAES(encryptMsg, secret)

      decryptMsg mustBe text
    }
  }

  "aesKey" in {
    println(Utils.randomString(64))
  }

  "2" in {
    println(0xff)

    println(10 + ('a' to 'z').length * 2)
  }

}
