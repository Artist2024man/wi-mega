package com.wuin.wi_mega.mega_market;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wuin.wi_mega.common.util.HttpClientUtils;
import com.wuin.wi_mega.common.util.StringUtils;
import com.wuin.wi_mega.mega_market.model.Signal;
import com.wuin.wi_mega.mega_market.model.SignalResult;
import com.wuin.wi_mega.mega_market.model.StrategySignalRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class MegaMarketClient {

    private static final Cache<String, SignalResult> CACHE = Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(512)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();

    private static final String REST_BASE = "http://127.0.0.1:8767";

    public static SignalResult requestSignal(Long sessionId, StrategySignalRequest request, Long start) {
        try {
            String cacheKey = buildCacheKey(request, start);
            SignalResult result = CACHE.getIfPresent(cacheKey);
            if (null == result) {
                String resp = HttpClientUtils.post(REST_BASE.concat("/api/indicator/strategy-signal"), JSON.toJSONString(request));
                log.warn("requestSignal -> sessionId={}, param={}, response={}", sessionId, JSON.toJSONString(request), resp);
                JSONObject object = JSON.parseObject(resp);
                if (object.getInteger("code") == 200) {
                    result = object.getJSONObject("data").toJavaObject(SignalResult.class);
                    CACHE.put(cacheKey, result);
                } else {
                    log.warn("requestSignal -> response code is not 200,  body: {}", JSON.toJSONString(request));
                    return new SignalResult(Signal.NONE, "请求指标服务响应的code不为200");
                }
            }
            return result;
        } catch (Throwable e) {
            log.error("requestSignal -> failed, body=" + JSON.toJSONString(request), e);
            return new SignalResult(Signal.NONE, "请求指标服务异常");
        }
    }

    private static String buildCacheKey(StrategySignalRequest request, Long start) {
        String md5 = StringUtils.encodeToMD5(JSON.toJSONString(request));
        return md5 + "_" + start;
    }


}

