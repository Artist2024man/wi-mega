package com.wuin.wi_mega.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SyncStatusEnum {
    NO_SYNC(0, "未同步"),
    FINISHED(1, "已同步"),
    ;

    private final Integer code;
    private final String message;

    SyncStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SyncStatusEnum byCode(Integer code) {
        return Arrays.stream(SyncStatusEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }
}
