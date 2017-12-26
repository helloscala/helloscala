package helloscala.util

import java.security.MessageDigest

import helloscala.test.HelloscalaSpec
import org.bouncycastle.util.encoders.Hex

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-05-15.
 */
class SecurityUtilsTest extends HelloscalaSpec {

  "SecurityUtilsTest" should {

    "sha1Hex" in {
      val v1 = SecurityUtils.sha1Hex("yangbajing")
      val v2 = SecurityUtils.sha1Hex("yangjing")
      v1 must not be v2
    }

    "sha1Hex 2" in {
      var sha = MessageDigest.getInstance("SHA1")
      sha.update("yangbajing".getBytes())
      val v1 = Hex.toHexString(sha.digest())

      sha = MessageDigest.getInstance("SHA1")
      sha.update("yangjing".getBytes())
      val v2 = Hex.toHexString(sha.digest())

      v1 must not be v2
    }

  }

  "sha256hex length" in {
    val len = SecurityUtils.sha256Hex("哈哈哈哈哈哈")
    println(len)
    println(len.length)
  }
}
