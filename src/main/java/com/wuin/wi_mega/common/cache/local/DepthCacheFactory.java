package com.wuin.wi_mega.common.cache.local;

import com.alibaba.fastjson2.JSONArray;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.binance.bo.DepthLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DepthCacheFactory {

    private final Map<SymbolEnum, DepthCache> cacheMap = new ConcurrentHashMap<>();

    public void clear(SymbolEnum symbol) {
        getCache(symbol).clear();
    }

    public void applyIncrement(SymbolEnum symbol, JSONArray bidArray, JSONArray askArray) {
        getCache(symbol).applyIncrement(bidArray, askArray);
    }

    /**
     * 按区间查询深度（含边界）。bids 为倒序，asks 为正序；此处按价格数值过滤，保持各自迭代顺序。
     */
    public List<DepthLevel> findRange(SymbolEnum symbol, BigDecimal min, BigDecimal max, boolean isBids) {
        return getCache(symbol).findRange(min, max, isBids);
    }

    public Boolean rangeIsOk(SymbolEnum symbol, BigDecimal min, BigDecimal max, BigDecimal minQty, boolean isBids) {
        return getCache(symbol).rangeIsOk(min, max, minQty, isBids);
    }

    private DepthCache getCache(SymbolEnum symbol) {
        DepthCache cache = cacheMap.get(symbol);
        if (null == cache) {
            synchronized (this) {
                cache = cacheMap.get(symbol);
                if (null == cache) {
                    cache = new DepthCache();
                    cacheMap.put(symbol, cache);
                }
            }
        }
        return cache;
    }

}
