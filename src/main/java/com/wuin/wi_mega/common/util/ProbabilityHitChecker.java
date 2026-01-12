package com.wuin.wi_mega.common.util;

import java.util.concurrent.ThreadLocalRandom;

public class ProbabilityHitChecker {

    /**
     * 根据概率判断是否命中
     *
     * @param probability 概率值，范围0-100
     *                    100表示100%返回true
     *                    90表示90%概率返回true
     *                    0表示0%概率返回true（总是返回false）
     * @return true表示命中，false表示未命中
     * @throws IllegalArgumentException 如果概率值不在0-100范围内
     */
    public static boolean isHit(int probability) {
        if (probability >= 100) {
            return true;
        }
        if (probability <= 0) {
            return false;
        }
        int randomValue = ThreadLocalRandom.current().nextInt(100);
        return randomValue < probability;
    }
}