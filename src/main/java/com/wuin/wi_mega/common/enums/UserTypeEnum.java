package com.wuin.wi_mega.common.enums;

import com.wuin.wi_mega.common.util.CompareUtils;
import lombok.Getter;

import java.util.Arrays;

/**
 * 链路类型枚举
 */
@Getter
public enum UserTypeEnum {
    ADMIN(0, "管理员"),
    NORMAL(1, "普通用户"),
    ;

    private final Integer code;
    private final String message;

    UserTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static UserTypeEnum byCode(Integer code) {
        return Arrays.stream(UserTypeEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalByCode(Integer code) {
        return CompareUtils.equals(code, getCode());
    }


    public Integer code() {
        return code;
    }
}
