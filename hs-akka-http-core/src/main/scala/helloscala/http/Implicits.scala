package helloscala.http

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import helloscala.common.exception.HSException
import helloscala.common.jackson.Jackson
import helloscala.common.util.StringUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object Implicits {

  implicit class FutureToObject(future: Future[HttpResponse]) {
    def to[T](implicit ev: ClassTag[T], mat: Materializer, ex: ExecutionContext): Future[Either[HSException, T]] = {
      future
        .flatMap(response => Unmarshal(response.entity).to[String].map(str => response.status -> str))
        .map {
          case (status, str) if okStatus(status.intValue()) =>
            if (StringUtils.isNoneBlank(str))
              Right(Jackson.defaultObjectMapper.readValue(str, ev.runtimeClass).asInstanceOf[T])
            else
              Right(str.asInstanceOf[T])
          case (status, str) =>
            Left(Jackson.defaultObjectMapper.readValue(str, classOf[HSException]).asInstanceOf[HSException])
        }
    }
  }

  private def okStatus(id: Int) = id >= 200 && id < 299

}
