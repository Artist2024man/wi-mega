package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum UserStatusEnum {
    NORMAL(1, "正常"),
    FORBIDDEN(2, "禁用"),
    ;

    private final Integer code;
    private final String message;

    UserStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static UserStatusEnum byCode(Integer code) {
        return Arrays.stream(UserStatusEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
