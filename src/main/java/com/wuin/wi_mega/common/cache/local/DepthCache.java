package com.wuin.wi_mega.common.cache.local;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.wuin.wi_mega.binance.bo.DepthLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 盘口缓存，封装 bids/asks 的有序并发存储与常用查询。
 */
@Slf4j
public class DepthCache {

    // 买盘：价格从高到低
    @Getter
    private final NavigableMap<BigDecimal, BigDecimal> bids = new ConcurrentSkipListMap<>(Collections.reverseOrder());
    // 卖盘：价格从低到高
    @Getter
    private final NavigableMap<BigDecimal, BigDecimal> asks = new ConcurrentSkipListMap<>();

    public void clear() {
        bids.clear();
        asks.clear();
    }

    public void applyIncrement(JSONArray bidArray, JSONArray askArray) {
        applyArray(bidArray, true);
        applyArray(askArray, false);
    }

    private void applyArray(JSONArray arr, boolean isBid) {
        if (arr == null) {
            return;
        }
        NavigableMap<BigDecimal, BigDecimal> target = isBid ? bids : asks;
        for (Object entry : arr) {
            JSONArray entryArr = (JSONArray) entry;
            if (entryArr == null || entryArr.size() < 2) continue;
            BigDecimal price = new BigDecimal(entryArr.getString(0));
            BigDecimal qty = new BigDecimal(entryArr.getString(1));
            if (qty.compareTo(BigDecimal.ZERO) == 0) {
                target.remove(price);
            } else {
                target.put(price, qty);
            }
        }
    }

    /**
     * 按区间查询深度（含边界）。bids 为倒序，asks 为正序；此处按价格数值过滤，保持各自迭代顺序。
     */
    public List<DepthLevel> findRange(BigDecimal min, BigDecimal max, boolean isBids) {
        // 使用 subMap 获取区间视图（包含边界）
        NavigableMap<BigDecimal, BigDecimal> subMap = findRangeInner(min, max, isBids);
        if (subMap == null) {
            return new ArrayList<>();
        }
        // 将结果转换为列表
        List<DepthLevel> result = new ArrayList<>(subMap.size());
        for (Map.Entry<BigDecimal, BigDecimal> entry : subMap.entrySet()) {
            result.add(new DepthLevel(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private NavigableMap<BigDecimal, BigDecimal> findRangeInner(BigDecimal min, BigDecimal max, boolean isBids) {
        NavigableMap<BigDecimal, BigDecimal> target = isBids ? bids : asks;

        // 边界检查
        if (target.isEmpty()) {
            return null;
        }

        // 确定起始和结束边界
        BigDecimal start = isBids ? (max != null ? max : target.firstKey())
                : (min != null ? min : target.firstKey());
        BigDecimal end = isBids ? (min != null ? min : target.lastKey())
                : (max != null ? max : target.lastKey());

        // 验证边界有效性
        if (start == null || end == null) {
            return null;
        }

        // 对于 bids（倒序），需要确保 start <= end（在原排序意义上）
        // 对于 asks（正序），也需要确保 start <= end
        if (!isValidRange(target, start, end)) {
            return null;
        }

        return target.subMap(start, true, end, true);
    }

    private boolean isValidRange(NavigableMap<BigDecimal, ?> map,
                                 BigDecimal from,
                                 BigDecimal to) {
        Comparator<? super BigDecimal> cmp = map.comparator();
        if (cmp == null) {
            return from.compareTo(to) <= 0;
        }
        return cmp.compare(from, to) <= 0;
    }



    public boolean rangeIsOk(BigDecimal min, BigDecimal max, BigDecimal minQty, boolean isBids) {
        NavigableMap<BigDecimal, BigDecimal> subMap = findRangeInner(min, max, isBids);
        log.warn("rangeIsOk -> min={}, max={}, minQty={}, isBids={}, subMap={}, bids.siz={}, asks.size={}", min, max, minQty, isBids, JSON.toJSONString(subMap), bids.size(), asks.size());
        if (null == subMap || subMap.isEmpty()) {
            return false;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal value : subMap.values()) {
            total = total.add(value);
            if (total.compareTo(minQty) >= 0) {
                return true;
            }
        }
        return false;
    }

}

