package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum SessionStatusEnum {
    WAIT_START(10, "待开始"),
    RUNNING(20, "运行中"),
    TAKE_PROFIT(30, "止盈"),
    STOP_LOSS(40, "止损"),

    STOP_HAND(50, "手动停止"),
    ;

    private final Integer code;
    private final String message;

    public static List<Integer> start() {
        return Arrays.asList(WAIT_START.code);
    }

    public static List<Integer> running() {
        return Arrays.asList(WAIT_START.code, RUNNING.code);
    }

    public static List<Integer> profiting() {
        return Arrays.asList(RUNNING.code);
    }

    public static List<Integer> completed() {
        return Arrays.asList(TAKE_PROFIT.code, STOP_LOSS.code, STOP_HAND.code);
    }

    SessionStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SessionStatusEnum byCode(Integer code) {
        return Arrays.stream(SessionStatusEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
