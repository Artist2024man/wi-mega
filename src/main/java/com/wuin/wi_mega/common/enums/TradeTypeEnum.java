package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TradeTypeEnum {

    MOCK(1, "模拟"),
    REAL(2, "实仓"),
    ;

    private final Integer code;
    private final String message;

    TradeTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static TradeTypeEnum byCode(Integer code) {
        return Arrays.stream(TradeTypeEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
