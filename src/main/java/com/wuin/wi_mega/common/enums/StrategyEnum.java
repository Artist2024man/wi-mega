package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum StrategyEnum {

    ONE_MIN_SHORT(1, "[超短线]一分钟双向"),
    MARTINGALE_APPEND(2, "高频马丁(补仓)"),
    MARTINGALE_SIGNAL(3, "高频马丁(信号驱动)"),
    ;

    private final Integer code;
    private final String message;

    StrategyEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static StrategyEnum byCode(Integer code) {
        return Arrays.stream(StrategyEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
