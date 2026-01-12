package com.wuin.wi_mega.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    public static String beauty(LocalDateTime time) {
        if (time == null) {
            return "无";
        }
        return beauty(time, "yyyy-MM-dd HH:mm:ss");
    }

    public static LocalDateTime parseDay(String dateTimeStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate date = LocalDate.parse(dateTimeStr, formatter);
        return date.atStartOfDay(); // 补上 00:00:00
    }

    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    public static String beauty(LocalDateTime time, String format) {
        if (time == null) {
            return "无";
        }
        DateTimeFormatter f = DateTimeFormatter.ofPattern(format);
        return time.format(f);
    }

    /**
     * 获取当前时间分钟开始的毫秒数
     * 例如：2025-12-12 12:32:16 -> 返回 2025-12-12 12:32:00 的毫秒数
     *
     * @return 当前时间分钟开始的毫秒数（从1970-01-01 00:00:00 UTC开始）
     */
    public static long getMinuteStartMillis() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minuteStart = now.withSecond(0).withNano(0);
        ZonedDateTime zonedDateTime = minuteStart.atZone(ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * 获取指定时间分钟开始的毫秒数
     *
     * @param time 指定的时间
     * @return 该时间分钟开始的毫秒数
     */
    public static long getMinuteStartMillis(LocalDateTime time) {
        LocalDateTime minuteStart = time.withSecond(0).withNano(0);
        ZonedDateTime zonedDateTime = minuteStart.atZone(ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public static long getPeriodStartMillis(LocalDateTime time, int minutes) {
        LocalDateTime truncated = time.truncatedTo(ChronoUnit.MINUTES);
        int floorMinute = (truncated.getMinute() / minutes) * minutes;
        return truncated
                .withMinute(floorMinute)
                .withSecond(0)
                .withNano(0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}
