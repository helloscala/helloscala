/*
 * Copyright 2017 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helloscala.common.aes;

import helloscala.common.exception.HSException;

@SuppressWarnings("serial")
public class AesException extends HSException {

    public final static int OK = 0;
    public final static int ValidateSignatureError = -40001;
    public final static int ParseXmlError = -40002;
    public final static int ComputeSignatureError = -40003;
    public final static int IllegalAesKey = -40004;
    public final static int ValidateAppidError = -40005;
    public final static int EncryptAESError = -40006;
    public final static int DecryptAESError = -40007;
    public final static int IllegalBuffer = -40008;
    //public final static int EncodeBase64Error = -40009;
    //public final static int DecodeBase64Error = -40010;
    //public final static int GenReturnXmlError = -40011;

    private static String getMessage(int code) {
        switch (code) {
            case ValidateSignatureError:
                return "签名验证错误";
            case ParseXmlError:
                return "xml解析失败";
            case ComputeSignatureError:
                return "sha加密生成签名失败";
            case IllegalAesKey:
                return "SymmetricKey非法";
            case ValidateAppidError:
                return "appid校验失败";
            case EncryptAESError:
                return "aes加密失败";
            case DecryptAESError:
                return "aes解密失败";
            case IllegalBuffer:
                return "解密后得到的buffer非法";
//		case EncodeBase64Error:
//			return "base64加密错误";
//		case DecodeBase64Error:
//			return "base64解密错误";
//		case GenReturnXmlError:
//			return "xml生成失败";
            default:
                return null; // cannot be
        }
    }

    AesException(Integer errCode) {
        super(errCode, getMessage(errCode));
    }

}
