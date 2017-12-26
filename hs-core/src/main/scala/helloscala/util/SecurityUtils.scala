/**
 * 安全，摘要算法相关工具
 *
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-27.
 */
package helloscala.util

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.MessageDigest
import java.util

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ FileIO, Sink }
import helloscala.common.Configuration
import helloscala.common.aes.HSBizMsgCrypt
import helloscala.common.util.StringUtils
import org.bouncycastle.util.encoders.Hex

import scala.concurrent.Future

case class ByteSaltPassword(salt: Array[Byte], saltPassword: Array[Byte])

case class SaltPassword(salt: String, saltPassword: String) {
  require(
    StringUtils.isNoneBlank(salt) && salt.length == SaltPassword.SALT_LENGTH,
    s"salt字符串长度必需为${SaltPassword.SALT_LENGTH}")
  require(
    StringUtils.isNoneBlank(saltPassword) && saltPassword.length == SaltPassword.SALT_PASSWORD_LENGTH,
    s"salt字符串长度必需为${SaltPassword.SALT_PASSWORD_LENGTH}")
}

object SaltPassword {
  val SALT_LENGTH = 12
  val SALT_PASSWORD_LENGTH = 64
}

object MessageDigestAlgorithms {
  /**
   * The MD5 message digest algorithm defined in RFC 1321.
   */
  val MD5 = "MD5"

  /**
   * The SHA-1 hash algorithm defined in the FIPS PUB 180-2.
   */
  val SHA_1 = "SHA-1"

  /**
   * The SHA-256 hash algorithm defined in the FIPS PUB 180-2.
   */
  val SHA_256 = "SHA-256"

  /**
   * The SHA-512 hash algorithm defined in the FIPS PUB 180-2.
   */
  val SHA_512 = "SHA-512"
}

object SecurityUtils extends SecurityUtils

class SecurityUtils {
  final val CLIENT_KEY_LENGTH = 32
  final val ENCODING_AES_KEY_LENGTH = 43

  def generateBizMsgCrypt(configuration: Configuration): HSBizMsgCrypt = {
    val key = configuration.getString("helloscala.crypt.client-key")
    val encodingAesKey = configuration.getString("helloscala.crypt.encoding-aes-key")
    val appId = configuration.getString("helloscala.crypt.client-id")
    new HSBizMsgCrypt(key, encodingAesKey, appId)
  }

  def generateBizMsgCrypt(key: String, encodingAesKey: String, clientId: String): HSBizMsgCrypt =
    new HSBizMsgCrypt(key, encodingAesKey, clientId)

  def digestMD5(): MessageDigest = {
    MessageDigest.getInstance(MessageDigestAlgorithms.MD5)
  }

  def digestSha1(): MessageDigest = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_1)

  def digestSha256(): MessageDigest = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256)

  def digestSha512(): MessageDigest = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_512)

  def md5(data: Array[Byte]): Array[Byte] = {
    val digest = digestMD5()
    digest.update(data)
    digest.digest()
  }

  def md5Hex(data: Array[Byte]): String = {
    Hex.toHexString(md5(data))
  }

  def md5Hex(data: String): String = {
    md5Hex(data.getBytes(StandardCharsets.UTF_8))
  }

  def sha1(data: Array[Byte]): Array[Byte] = {
    val digest = digestSha1()
    digest.update(data)
    digest.digest()
  }

  def sha1Hex(data: Array[Byte]): String = {
    Hex.toHexString(sha1(data))
  }

  def sha1Hex(data: String): String = {
    sha1Hex(data.getBytes(StandardCharsets.UTF_8))
  }

  def sha256(data: Array[Byte]): Array[Byte] = {
    val digest = digestSha256()
    digest.update(data)
    digest.digest()
  }

  def sha256Hex(data: Array[Byte]): String = {
    Hex.toHexString(sha256(data))
  }

  def sha256Hex(data: String): String = {
    sha256Hex(data.getBytes(StandardCharsets.UTF_8))
  }

  def sha512(data: Array[Byte]): Array[Byte] = {
    val digest = digestSha512()
    digest.update(data)
    digest.digest()
  }

  def sha512Hex(data: Array[Byte]): String = {
    Hex.toHexString(sha512(data))
  }

  def sha512Hex(data: String): String = {
    sha512Hex(data.getBytes(StandardCharsets.UTF_8))
  }

  def reactiveSha256Hex(path: Path)(implicit mat: ActorMaterializer): Future[String] = {
    import mat.executionContext
    reactiveSha256(path).map(bytes => Hex.toHexString(bytes))
  }

  def reactiveSha256(path: Path)(implicit mat: ActorMaterializer): Future[Array[Byte]] = {
    import mat.executionContext
    val md = SecurityUtils.digestSha256()
    FileIO
      .fromPath(path)
      .map(bytes => md.update(bytes.asByteBuffer))
      .runWith(Sink.ignore)
      .map(_ => md.digest())
  }

  /**
   * 生成通用 Salt 及 Salt Password
   *
   * @param password 待生成密码
   * @return
   */
  def byteGeneratePassword(password: String): ByteSaltPassword = {
    val salt = Utils.randomBytes(SaltPassword.SALT_LENGTH)
    val saltPwd = sha256(salt ++ password.getBytes)
    ByteSaltPassword(salt, saltPwd)
  }

  def generatePassword(password: String): SaltPassword = {
    val salt = Utils.randomString(SaltPassword.SALT_LENGTH)
    val saltPwd = sha256Hex(salt ++ password)
    SaltPassword(salt, saltPwd)
  }

  /**
   * 校验密码
   *
   * @param salt     salt
   * @param saltPwd  salt password
   * @param password request password
   * @return
   */
  def matchSaltPassword(salt: Array[Byte], saltPwd: Array[Byte], password: Array[Byte]): Boolean = {
    val bytes = sha256(salt ++ password)
    util.Arrays.equals(saltPwd, bytes)
  }

  def matchSaltPassword(salt: String, saltPwd: String, password: String): Boolean = {
    val securityPassword = sha256Hex(salt + password)
    securityPassword == saltPwd
  }

}
