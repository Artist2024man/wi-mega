package com.wuin.wi_mega.common.exception;

import com.wuin.wi_mega.common.constants.AppConstant;
import com.wuin.wi_mega.common.resp.RespModel;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Hidden
@ControllerAdvice
@Slf4j
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RespModel all(Exception e) {
        log.error("[sys eception]", e);
        return error(AppConstant.UNKNOW_ERROR_CODE, AppConstant.UNKNOW_ERROR_MESSAGE);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(APIRuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RespModel apiRuntimeException(APIRuntimeException e) {
        log.warn("[biz exception]code={}, mess:{}", e.getCode(), e.getMessage());
        return error(e.getCode(), e.getMessage());
    }

    private RespModel error(Integer code, Object data) {
        return RespModel.error(code, data);
    }

}
