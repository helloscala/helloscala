package helloscala.common.exception

import helloscala.common.ErrCodes
import io.swagger.annotations.ApiModelProperty

import scala.annotation.meta.field

case class HSAcceptedWarning(
  errMsg: String,
  @(ApiModelProperty @field)(dataType = "object") data: AnyRef = null,
  errCode: Int = ErrCodes.ACCEPTED,
  cause: Throwable = null) extends HSException(errCode, errMsg, cause) {
  httpStatus = ErrCodes.ACCEPTED
  if (data != null) setData(data)
}

case class HSBadRequestException(
  errMsg: String,
  @(ApiModelProperty @field)(dataType = "object") data: AnyRef = null,
  errCode: Int = ErrCodes.BAD_REQUEST,
  cause: Throwable = null) extends HSException(errCode, errMsg, cause) {
  httpStatus = ErrCodes.BAD_REQUEST
  if (data != null) setData(data)
}

case class HSUnauthorizedException(
  errMsg: String,
  @(ApiModelProperty @field)(dataType = "object") data: AnyRef = null,
  errCode: Int = ErrCodes.UNAUTHORIZED,
  cause: Throwable = null) extends HSException(errCode, errMsg, cause) {
  httpStatus = ErrCodes.UNAUTHORIZED
  if (data != null) setData(data)
}

case class HSNoContentException(
  errMsg: String,
  @(ApiModelProperty @field)(dataType = "object") data: AnyRef = null,
  errCode: Int = ErrCodes.NO_CONTENT,
  cause: Throwable = null) extends HSException(errCode, errMsg, cause) {
  httpStatus = ErrCodes.NO_CONTENT
  if (data != null) setData(data)
}

case class HSForbiddenException(
  errMsg: String,
  @(ApiModelProperty @field)(dataType = "object") data: AnyRef = null,
  errCode: Int = ErrCodes.FORBIDDEN,
  cause: Throwable = null) extends HSException(errCode, errMsg, cause) {
  httpStatus = ErrCodes.FORBIDDEN
  if (data != null) setData(data)
}

case class HSNotFoundException(
  errMsg: String,
  @(ApiModelProperty @field)(dataType = "object") data: AnyRef = null,
  errCode: Int = ErrCodes.NOT_FOUND,
  cause: Throwable = null) extends HSException(errCode, errMsg, cause) {
  httpStatus = ErrCodes.NOT_FOUND
  if (data != null) setData(data)
}

case class HSConflictException(
  errMsg: String,
  @(ApiModelProperty @field)(dataType = "object") data: AnyRef = null,
  errCode: Int = ErrCodes.CONFLICT,
  cause: Throwable = null) extends HSException(errCode, errMsg, cause) {
  httpStatus = ErrCodes.CONFLICT
  if (data != null) setData(data)
}

case class HSNotImplementedException(
  errMsg: String,
  @(ApiModelProperty @field)(dataType = "object") data: AnyRef = null,
  errCode: Int = ErrCodes.NOT_IMPLEMENTED,
  cause: Throwable = null) extends HSException(errCode, errMsg, cause) {
  httpStatus = ErrCodes.NOT_IMPLEMENTED
  if (data != null) setData(data)
}

case class HSInternalErrorException(
  errMsg: String,
  @(ApiModelProperty @field)(dataType = "object") data: AnyRef = null,
  errCode: Int = ErrCodes.INTERNAL_ERROR,
  cause: Throwable = null) extends HSException(errCode, errMsg, cause) {
  httpStatus = ErrCodes.INTERNAL_ERROR
  if (data != null) setData(data)
}
