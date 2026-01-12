package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DualSidePositionEnum {
    SINGLE(1, "单向持仓"),
    DOUBLE(2, "双向持仓"),

    ;

    private final Integer code;
    private final String message;

    DualSidePositionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static DualSidePositionEnum byCode(Integer code) {
        return Arrays.stream(DualSidePositionEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
