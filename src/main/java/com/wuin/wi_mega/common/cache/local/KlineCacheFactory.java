package com.wuin.wi_mega.common.cache.local;

import com.wuin.wi_mega.binance.bo.Kline;
import com.wuin.wi_mega.common.enums.KlineIntervalEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.common.util.DateUtils;
import com.wuin.wi_mega.queue.impl.SymbolPriceQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class KlineCacheFactory {

    @Autowired
    private SymbolPriceQueue symbolPriceQueue;

    private final Map<String, KLineCache> cacheMap = new ConcurrentHashMap<>();

    public void updateCurPrice(SymbolEnum symbol, Kline kline) {
        this.getCache(symbol, kline.getInterval()).setCurPrice(kline.getClose());
        symbolPriceQueue.offer(symbol.name());
    }

    public BigDecimal getCurPrice(SymbolEnum symbol, KlineIntervalEnum interval) {
        return this.getCache(symbol, interval).getCurPrice();
    }

    public void forceAdd(SymbolEnum symbol, Kline kline) {
        this.getCache(symbol, kline.getInterval()).forceAdd(kline);
    }

    public void add(SymbolEnum symbol, Kline kline) {
        this.getCache(symbol, kline.getInterval()).add(kline);
    }

    public Kline get(SymbolEnum symbol, KlineIntervalEnum interval, Long start) {
        return this.getCache(symbol, interval).get(start);
    }

    public BigDecimal getMaxSignedDiff(SymbolEnum symbol, KlineIntervalEnum interval, Kline cur, LocalDateTime now, int preN) {

        if (cur == null) {
            return BigDecimal.ZERO;
        }

        List<Long> keys = new ArrayList<>(preN);
        for (int i = 1; i <= preN; i++) {
            Long preMills = DateUtils.getPeriodStartMillis(now.minusMinutes((long) interval.getInterval() * i), interval.getInterval());
            keys.add(preMills);
        }

        Map<Long, Kline> res = getCache(symbol, interval).getAll(keys);

        BigDecimal high = null;
        BigDecimal low = null;

        for (Kline value : res.values()) {
            // 区间最高价
            if (value == null) {
                continue;
            }

            BigDecimal open = value.getOpen();
            BigDecimal close = value.getClose();

            if (open == null || close == null) {
                continue;
            }

            BigDecimal bodyHigh = open.max(close);
            BigDecimal bodyLow = open.min(close);

            if (high == null || bodyHigh.compareTo(high) > 0) {
                high = bodyHigh;
            }

            if (low == null || bodyLow.compareTo(low) < 0) {
                low = bodyLow;
            }
        }

        if (high == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal diffByHigh = cur.getClose().subtract(high);
        BigDecimal diffByLow = cur.getClose().subtract(low);
        return diffByLow.abs().compareTo(diffByHigh.abs()) > 0 ? diffByLow : diffByHigh;
    }

    private KLineCache getCache(SymbolEnum symbol, KlineIntervalEnum interval) {
        String key = buildKey(symbol, interval);
        KLineCache cache = cacheMap.get(key);
        if (null == cache) {
            synchronized (this) {
                cache = cacheMap.get(key);
                if (null == cache) {
                    switch (interval) {
                        case MINUTE_1:
                            cache = new KLineCache(1440, 2880, 24, TimeUnit.HOURS);
                            cacheMap.put(key, cache);
                            break;
                        case MINUTE_5:
                            cache = new KLineCache(288, 576, 24, TimeUnit.HOURS);
                            cacheMap.put(key, cache);
                            break;
                        default:
                            throw new IllegalArgumentException("系统暂时没有支持当前间隔的K线数据同步: " + interval);
                    }
                }
            }
        }
        return cache;
    }

    private String buildKey(SymbolEnum symbol, KlineIntervalEnum interval) {
        return symbol.toString() + "@" + interval.toString();
    }

    public Map<Long, Kline> getAll(SymbolEnum symbol, KlineIntervalEnum interval, List<Long> keys) {
        return this.getCache(symbol, interval).getAll(keys);
    }

    public List<Kline> getPreN(SymbolEnum symbol, KlineIntervalEnum interval, LocalDateTime cur, int preN) {
        List<Long> keys = new ArrayList<>(preN);
        for (int i = 1; i <= preN; i++) {
            Long preMills = DateUtils.getPeriodStartMillis(cur.minusMinutes((long) interval.getInterval() * i), interval.getInterval());
            keys.add(preMills);
        }
        Map<Long, Kline> preKlines = this.getAll(symbol, interval, keys);
        if (preKlines == null) {
            return null;
        }
        List<Kline> klineList = new ArrayList<>(preKlines.values());
        klineList.sort(Comparator.comparing(Kline::getStart)); //根据时间排序
        return klineList;
    }
}
