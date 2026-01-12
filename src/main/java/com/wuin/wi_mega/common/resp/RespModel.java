package com.wuin.wi_mega.common.resp;

import com.wuin.wi_mega.common.constants.AppConstant;
import lombok.Data;

@Data
public class RespModel {

    private int code;

    private boolean success;

    private String description;

    private Object data;

    private String requestId;

    public static RespModel error(String desc) {
        return error(AppConstant.UNKNOW_ERROR_CODE, desc);
    }

    public static RespModel error(Integer code, Object data) {
        return build(code, Boolean.FALSE, AppConstant.ERROR_MESSAGE, data);
    }

    public static RespModel success(Object data) {
        return build(AppConstant.SUCCESS_CODE, Boolean.TRUE, AppConstant.SUCCESS_MESS, data);
    }

    public static RespModel success() {
        return build(AppConstant.SUCCESS_CODE, Boolean.TRUE, AppConstant.SUCCESS_MESS, null);
    }

    private static RespModel build(Integer code, Boolean success, String message, Object data) {
        RespModel model = new RespModel();
        model.setCode(code);
        model.setSuccess(success);
        model.setDescription(message);
        model.setData(data);
        return model;
    }


}
