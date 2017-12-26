package helloscala.util

import helloscala.common.types.ObjectId
import helloscala.test.HelloscalaSpec

class UtilsTest extends HelloscalaSpec {

  "UtilsTest" should {

    "swap" in {
      var x = 2
      var y = 3
      Utils.swap(x, y) match { case t => x = t._1; y = t._2 }
      println(s"x: $x, y: $y")
    }

    "43" in {
      println(Utils.randomString(SecurityUtils.CLIENT_KEY_LENGTH))
      println(Utils.randomString(SecurityUtils.ENCODING_AES_KEY_LENGTH))

      println(ObjectId.generate())
    }
  }

}
