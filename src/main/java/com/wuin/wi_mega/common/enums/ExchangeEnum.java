package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ExchangeEnum {
    BINANCE(1, "BINANCE"),
    ;

    private final Integer code;
    private final String message;

    ExchangeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ExchangeEnum byCode(Integer code) {
        return Arrays.stream(ExchangeEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
