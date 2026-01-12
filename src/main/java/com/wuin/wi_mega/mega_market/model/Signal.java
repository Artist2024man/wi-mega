package com.wuin.wi_mega.mega_market.model;

import lombok.Getter;

/**
 * 交易信号枚举
 */
@Getter
public enum Signal {
    NONE("无信号"),
    LONG("做多"),
    SHORT("做空"),
    CLOSE_LONG("平多"),
    CLOSE_SHORT("平空");

    private final String description;

    public static Boolean isOpenSignal(Signal signal) {
        return LONG.equals(signal) || SHORT.equals(signal);
    }

    Signal(String description) {
        this.description = description;
    }
}

