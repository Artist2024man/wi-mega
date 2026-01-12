package com.wuin.wi_mega.binance.market;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.wuin.wi_mega.binance.bo.Kline;
import com.wuin.wi_mega.common.cache.local.KlineCacheFactory;
import com.wuin.wi_mega.common.enums.KlineIntervalEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.common.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KlineBackFillService {

    private static final String REST_BASE = "https://fapi.binance.com";
    private static final String CONTRACT_TYPE = "PERPETUAL";

    @Autowired
    private KlineCacheFactory klineCacheFactory;

    public void fetchAndCache(String symbol, KlineIntervalEnum interval, long startInclusiveMs, long endInclusiveMs, int limit) throws Exception {
        String url = REST_BASE + "/fapi/v1/continuousKlines"
                + "?pair=" + symbol
                + "&contractType=" + CONTRACT_TYPE
                + "&interval=" + interval.getCodeStr()
                + "&startTime=" + startInclusiveMs
                + "&endTime=" + endInclusiveMs
                + "&limit=" + limit;
        String resp = HttpClientUtils.get(url);
        JSONArray arr = JSON.parseArray(resp);
        if (arr == null) {
            log.warn("fetchAndCache -> K线响应为空，url={}", url);
            return;
        }
        for (Object item : arr) {
            Kline k = new Kline((JSONArray) item, symbol, interval);
            klineCacheFactory.forceAdd(SymbolEnum.valueOf(symbol), k);
        }
    }
}

