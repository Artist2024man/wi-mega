package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum StrategyStatusEnum {
    RUNNING(1, "运行中"),
    STOP(0, "停止"),
    ;

    private final Integer code;
    private final String message;

    StrategyStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static StrategyStatusEnum byCode(Integer code) {
        return Arrays.stream(StrategyStatusEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
