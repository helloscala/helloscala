package helloscala.common.data;

/**
 * Api Response Result
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-01.
 */
public class ApiResult implements IApiResult<Object> {
    private Integer errCode = 0;
    private String errMsg = null;
    private Object data = null;

    public ApiResult() {

    }

    public ApiResult(Integer errCode, String errMsg, Object data) {
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.data = data;
    }

    public static ApiResult success() {
        return success(null);
    }

    public static ApiResult success(Object data) {
        return new ApiResult(0, null, data);
    }

    public static ApiResult error(Integer errCode, String errMsg) {
        return error(errCode, errMsg, null);
    }

    public static ApiResult error(Integer errCode, String errMsg, Object data) {
        return new ApiResult(errCode, errMsg, data);
    }

    public Integer getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ApiResult{" +
                "errCode=" + errCode +
                ", errMsg='" + errMsg + '\'' +
                ", data=" + data +
                '}';
    }
}
