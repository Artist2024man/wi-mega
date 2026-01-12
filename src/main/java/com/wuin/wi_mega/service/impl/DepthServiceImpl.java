package com.wuin.wi_mega.service.impl;

import com.wuin.wi_mega.common.cache.local.DepthCacheFactory;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.binance.bo.DepthLevel;
import com.wuin.wi_mega.service.DepthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DepthServiceImpl implements DepthService {

    @Autowired
    private DepthCacheFactory depthCacheFactory;

    /**
     * 依据目标价与允许浮动区间，从盘口中选取“价格有利 + 排队少”的综合最优价。
     * BUY 使用 bids（倒序）；SELL 使用 asks（正序）。
     */
    public BigDecimal getPrefectPrice(SymbolEnum symbol, BigDecimal price, BigDecimal stepPrice, boolean isBuy) {
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(stepPrice, "stepPrice");
        if (stepPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }
        BigDecimal min = price.subtract(stepPrice);
        BigDecimal max = price.add(stepPrice);

        List<DepthLevel> levels = depthCacheFactory.findRange(symbol, min, max, isBuy); // bids 为倒序，asks 为正序
        if (levels.isEmpty()) {
            return price;
        }

        // 计算累计排队量（bids 从高到低累加，asks 从低到高累加）
        double[] cumulative = new double[levels.size()];
        double sum = 0d;
        for (int i = 0; i < levels.size(); i++) {
            sum += levels.get(i).getQuantity().doubleValue();
            cumulative[i] = sum;
        }
        double maxCum = cumulative[cumulative.length - 1];
        if (maxCum <= 0) {
            return price;
        }

        double bestScore = -1;
        BigDecimal bestPrice = levels.get(0).getPrice();

        double range = max.subtract(min).doubleValue();
        for (int i = 0; i < levels.size(); i++) {
            DepthLevel lvl = levels.get(i);
            double p = lvl.getPrice().doubleValue();
            double cum = cumulative[i];

            // 价格贴近度：BUY 越低越好；SELL 越高越好
            double priceScore = isBuy
                    ? (max.doubleValue() - p) / range
                    : (p - min.doubleValue()) / range;
            if (priceScore < 0) priceScore = 0;
            if (priceScore > 1) priceScore = 1;

            // 排队惩罚：累计量越大，得分越低
            double queuePenalty = 1.0 / (1.0 + cum / maxCum); // ∈ (0,1]

            // 综合评分
            double score = 0.6 * priceScore + 0.4 * queuePenalty;

            // 选更高分；分数相同则更贴近目标价；若仍相同，BUY 取更低，SELL 取更高
            if (score > bestScore
                    || (Math.abs(score - bestScore) < 1e-9 && Math.abs(p - price.doubleValue()) < Math.abs(bestPrice.doubleValue() - price.doubleValue()))
                    || (Math.abs(score - bestScore) < 1e-9 && Math.abs(p - price.doubleValue()) == Math.abs(bestPrice.doubleValue() - price.doubleValue())
                    && ((isBuy && lvl.getPrice().compareTo(bestPrice) < 0) || (!isBuy && lvl.getPrice().compareTo(bestPrice) > 0)))) {
                bestScore = score;
                bestPrice = lvl.getPrice();
            }
        }
//        log.warn("calPrice -> levels:{}, bestPrice:{}, orign={}", JSON.toJSONString(levels), bestPrice, price);
        return bestPrice;
    }

}
