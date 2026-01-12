package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum StrategyInstanceOpenStatusEnum {


    NONE(1, "未开仓"),
    OPEN(2, "已开仓"),
    OPEN_AGAIN(3, "已补仓"),
    OPEN_REVERSE(4, "已开对冲"),
    ;

    private final Integer code;
    private final String message;

    StrategyInstanceOpenStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static StrategyInstanceOpenStatusEnum byCode(Integer code) {
        return Arrays.stream(StrategyInstanceOpenStatusEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
