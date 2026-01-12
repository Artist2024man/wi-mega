package com.wuin.wi_mega.common.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wuin.wi_mega.binance.bo.Kline;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class BinanceApiWeightCache {

    private final Integer MAX;

    private final AtomicInteger used = new AtomicInteger(0);

    private final Cache<Long, Integer> WEIGHT_CACHE;

    public BinanceApiWeightCache(int max) {
        this.MAX = max;
        WEIGHT_CACHE = Caffeine.newBuilder()
                .initialCapacity(128)
                .maximumSize(1024)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .removalListener((k, value, cause) -> {
                    if (null != value) {
                        used.addAndGet(-((Integer) value));
                    }
                })
                .build();
    }

    public int use(int weight) {
        WEIGHT_CACHE.put(SimpleSnowflake.nextId(), weight);
        return used.addAndGet(weight);
    }

    public Integer used() {
        return used.get();
    }
}
