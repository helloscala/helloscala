package helloscala.http.apitoken

import akka.http.scaladsl.server.Rejection
import helloscala.common.auth.ApiTokenInput
import helloscala.common.exception.HSException

import scala.concurrent.Future

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-30.
 */
trait TokenStorage[T] {
  /**
   * 判断 ApiTokenInput 是否有效
   *
   * @param in
   * @return
   */
  def lookup(in: ApiTokenInput): Future[Either[HSException, ApiTokenInput]]

  /**
   * 校验 Api Token 认证会话，并转换成 T 类型
   *
   * @return
   */
  def authorization(apiToken: ApiTokenInput): Future[Either[Rejection, T]]
}
