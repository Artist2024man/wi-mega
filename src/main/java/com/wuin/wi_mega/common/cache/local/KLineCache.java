package com.wuin.wi_mega.common.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wuin.wi_mega.binance.bo.Kline;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class KLineCache {

    @Getter
    @Setter
    private BigDecimal curPrice;

    private final Cache<Long, Kline> KLINE_CACHE;

    public KLineCache(int initCapacity, int maxSize, int duration, TimeUnit timeUnit) {
        KLINE_CACHE = Caffeine.newBuilder()
                .initialCapacity(initCapacity)
                .maximumSize(maxSize)
                .expireAfterAccess(duration, timeUnit) //最后访问一天后过期
                .build();
    }

    public void forceAdd(Kline kline) {
        KLINE_CACHE.put(kline.getStart(), kline);
    }

    public void add(Kline kline) {
        if (null == get(kline.getStart())) {
            forceAdd(kline);
        }
    }

    public Kline get(Long start) {
        if (start == null) {
            return null;
        }
        return KLINE_CACHE.getIfPresent(start);
    }

    public Map<Long, Kline> getAll(List<Long> starts) {
        if (starts == null) {
            log.warn("KLineCache -> starts is null, just return null");
            return null;
        }
        return KLINE_CACHE.getAllPresent(starts);
    }

    public void remove(Long start) {
        if (start == null) {
            log.warn("KLineCache -> start is null, just return");
            return;
        }
        KLINE_CACHE.invalidate(start);
    }
}
