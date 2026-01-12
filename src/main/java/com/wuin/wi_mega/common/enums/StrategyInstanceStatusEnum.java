package com.wuin.wi_mega.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StrategyInstanceStatusEnum {

    ONLINE(1, "已上架"),
    OFFLINE(2, "已下架");

    private final Integer code;
    private final String name;

    public static String getNameByCode(Integer code) {
        if (code == null) {
            return "未知";
        }
        for (StrategyInstanceStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getName();
            }
        }
        return "未知";
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public static StrategyInstanceStatusEnum getByCode(Integer code) {
        for (StrategyInstanceStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
