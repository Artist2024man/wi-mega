package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MockDataEnum {
    MOCK(1, "模拟数据"),
    REAL(0, "真实数据"),
    ;

    private final Integer code;
    private final String message;

    MockDataEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static MockDataEnum byCode(Integer code) {
        return Arrays.stream(MockDataEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
