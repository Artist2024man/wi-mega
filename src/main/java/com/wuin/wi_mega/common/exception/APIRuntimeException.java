// All rights reserved
package com.wuin.wi_mega.common.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * @version 1.0
 * @created 2023/3/2 下午3:19
 **/
public class APIRuntimeException extends RuntimeException implements IBizException {
    private Integer code;
    private String message;

    public APIRuntimeException(IResponseStatusMsg responseStatus, Throwable cause) {
        super(cause);
        this.code = responseStatus.getCode();
        this.message = StringUtils.isEmpty(cause.getMessage()) ? responseStatus.getMessage() : cause.getMessage();
    }

    public APIRuntimeException(IResponseStatusMsg responseStatus) {
        this.code = responseStatus.getCode();
        this.message = responseStatus.getMessage();
    }

    public APIRuntimeException(IResponseStatusMsg responseStatus, String message) {
        this.code = responseStatus.getCode();
        this.message = StringUtils.isEmpty(message) ? responseStatus.getMessage() : message;
    }

    public APIRuntimeException(Integer error, String message) {
        this.code = error;
        this.message = message;
    }

    public APIRuntimeException(Throwable cause) {
        super(cause);
        if (cause instanceof IBizException) {
            this.code = ((IBizException) cause).getCode();
        } else {
            this.code = IResponseStatusMsg.APIEnum.SERVER_ERROR.getCode();
        }
        this.message = StringUtils.isEmpty(cause.getMessage()) ?
                IResponseStatusMsg.APIEnum.SERVER_ERROR.getMessage() :
                cause.getMessage();
    }

    public APIRuntimeException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof IBizException) {
            this.code = ((IBizException) cause).getCode();
        } else {
            this.code = IResponseStatusMsg.APIEnum.SERVER_ERROR.getCode();
        }
        this.message = message;
    }

    public APIRuntimeException(IResponseStatusMsg responseStatus, String message, Throwable cause) {
        super(message, cause);
        this.code = responseStatus.getCode();
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
