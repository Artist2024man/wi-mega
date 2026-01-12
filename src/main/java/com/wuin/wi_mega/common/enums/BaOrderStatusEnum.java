package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BaOrderStatusEnum {
    NEW(1, "新建订单"),
    PARTIALLY_FILLED(2, "部分成交"),
    FILLED(3, "全部成交"),
    CANCELED(4, "已撤销"),
    REJECTED(5, "订单被拒绝"),
    EXPIRED(6, "订单过期"),
    EXPIRED_IN_MATCH(7, "订单被STP过期"),
    ;

    private final Integer code;
    private final String message;

    BaOrderStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static BaOrderStatusEnum byCode(Integer code) {
        return Arrays.stream(BaOrderStatusEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
