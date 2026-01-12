package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum SessionBizStatusEnum {
    WAIT_START(1, "待开仓(开仓异常)"),
    START(2, "开仓(待成交)"),
    WAIT_PROFIT(3, "待止盈or对冲"),
//    WAIT_APPEND_2_PROFIT(4, "待止盈or二次加仓"),
    WAIT_APPEND_PROFIT(5, "待止盈or对冲"),
    HOLD_ALL(6, "对冲(请手动处理)"),

//    CANCELED(7, "已撤单"),

//    EXCEPTION(8, "异常单"),

    TAKE_PROFIT(9, "已止盈"),

    STOP_LOSS(10, "已止损"),

//    STOP_HAND(11, "手动停止"),
    ;

    private final Integer code;
    private final String message;

    public static List<Integer> start() {
        return Arrays.asList(WAIT_START.code, START.code);
    }

    public static List<Integer> profiting() {
        return Arrays.asList(WAIT_PROFIT.code, WAIT_APPEND_PROFIT.code);
    }

    public static List<Integer> running() {
        return Arrays.asList(WAIT_START.code, START.code, WAIT_PROFIT.code, WAIT_APPEND_PROFIT.code, HOLD_ALL.code);
    }

    public static List<Integer> completed() {
        return Arrays.asList(TAKE_PROFIT.code, STOP_LOSS.code);
    }

    SessionBizStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SessionBizStatusEnum byCode(Integer code) {
        return Arrays.stream(SessionBizStatusEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
