// All rights reserved
package com.wuin.wi_mega.common.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @version 1.0
 * @created 2023/3/2 下午3:15
 **/
public class CompareUtils {
    public static boolean equals(Integer var1, Integer var2) {
        if (var1 == null && var2 == null) {
            return true;
        }
        return var1 == null ? var2 == null : var1.equals(var2);
    }

    public static boolean equals(Integer var1, Long var2) {
        if (var1 == null && var2 == null) {
            return true;
        }
        return var1 == null ? var2 == null : Long.valueOf(var1).equals(var2);
    }

    public static boolean equals(Long var1, Integer var2) {
        return equals(var2, var1);
    }

    public static boolean equals(BigDecimal var1, BigDecimal var2) {
        if (var1 == null && var2 == null) {
            return true;
        }
        return var1 != null && var2 != null && var1.compareTo(var2) == 0;
    }

    public static boolean equals(LocalDateTime var1, LocalDateTime var2) {
        if (var1 == null && var2 == null) {
            return true;
        }
        return var1 != null && var2 != null && var1.isEqual(var2);
    }

    public static boolean greaterEquals(Integer var1, Integer var2) {
        return var1 != null && var2 != null && var1.compareTo(var2) >= 0;
    }

    public static boolean greater(Integer var1, Integer var2) {
        return var1 != null && var2 != null && var1.compareTo(var2) > 0;
    }

    public static boolean greater(BigDecimal var1, BigDecimal var2) {
        return var1 != null && var2 != null && var1.compareTo(var2) > 0;
    }

    public static boolean greater(LocalDateTime var1, LocalDateTime var2) {
        return var1 != null && var2 != null && var1.isAfter(var2);
    }

    public static boolean less(LocalDateTime var1, LocalDateTime var2) {
        return var1 != null && var2 != null && var1.isBefore(var2);
    }

    public static boolean lessEquals(Integer var1, Integer var2) {
        return var1 != null && var2 != null && var1.compareTo(var2) <= 0;
    }

    public static boolean less(Integer var1, Integer var2) {
        return var1 != null && var2 != null && var1.compareTo(var2) < 0;
    }

    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        return obj1 == null ? obj2 == null : obj1.equals(obj2);
    }

    public static boolean between(Integer value, Integer rangeStart, Integer rangeEnd) {
        return lessEquals(rangeStart, value) && lessEquals(value, rangeEnd);
    }

    public static <T extends Comparable> T max(T var1, T var2) {
        if (var1.compareTo(var2) > 0) {
            return var1;
        }
        return var2;
    }

    public static <T extends Comparable> T min(T var1, T var2) {
        if (var2.compareTo(var1) > 0) {
            return var1;
        }
        return var2;
    }
}