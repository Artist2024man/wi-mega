package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AccountOrderTypeEnum {
    OPEN(1, "开仓"),
    CLOSE(2, "平仓"),
    ;

    private final Integer code;
    private final String message;

    AccountOrderTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static AccountOrderTypeEnum byCode(Integer code) {
        return Arrays.stream(AccountOrderTypeEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
