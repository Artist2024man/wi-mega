package com.wuin.wi_mega.common.enums;

import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

@Getter
public enum KlineIntervalEnum {
    MINUTE_1(1, "1m", 1),
    MINUTE_5(2, "5m", 5),
    MINUTE_15(3, "15m", 15),
    MINUTE_30(4, "30m", 30),
    HOUR_1(5, "1h", 60),
    HOUR_2(6, "2h", 120),
    HOUR_4(7, "4h", 240),
    HOUR_6(8, "6h", 360),
    HOUR_8(9, "8h", 480),
    HOUR_12(10, "12h", 720),
    DAY_1(11, "1d", 1440),
    DAY_3(12, "3d", 4320),
    WEEK_1(13, "1w", 10080),
    MONTH_1(14, "1M", 0),
    ;

    private final Integer code;
    private final String codeStr;
    private final int interval;

    KlineIntervalEnum(Integer code, String codeStr, int interval) {
        this.code = code;
        this.codeStr = codeStr;
        this.interval = interval;
    }

    public static KlineIntervalEnum byCode(Integer code) {
        return Arrays.stream(KlineIntervalEnum.values()).filter(en -> en.code.equals(code))
                .findFirst().orElse(null);
    }

    public static KlineIntervalEnum byCodeStr(String codeStr) {
        return Arrays.stream(KlineIntervalEnum.values()).filter(en -> en.getCodeStr().equals(codeStr))
                .findFirst().orElse(null);
    }

    public boolean equalsByCode(Integer code) {
        return this.code.equals(code);
    }

    public Integer code() {
        return code;
    }

    @JsonCreator
    public static KlineIntervalEnum fromValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;  // 空字符串返回 null
        }
        // 先尝试按 codeStr 匹配（如 "1m", "5m"）
        return Arrays.stream(KlineIntervalEnum.values())
                .filter(e -> e.getCodeStr().equalsIgnoreCase(value))
                .findFirst()
                // 再尝试按枚举名称匹配（如 "MINUTE_1"）
                .orElseGet(() -> Arrays.stream(KlineIntervalEnum.values())
                        .filter(e -> e.name().equalsIgnoreCase(value))
                        .findFirst()
                        .orElse(null));
    }
}
