package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SymbolEnum {
    ETHUSDT(1, "ETHUSDT"),
    ETHUSDC(2, "ETHUSDC"),
    ZECUSDT(3, "ZECUSDT"),
    ZECUSDC(4, "ZECUSDC"),
    SOLUSDT(5, "SOLUSDT"),
    SOLUSDC(6, "SOLUSDC"),
    ;

    private final Integer code;
    private final String message;

    SymbolEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SymbolEnum byCode(Integer code) {
        return Arrays.stream(SymbolEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
