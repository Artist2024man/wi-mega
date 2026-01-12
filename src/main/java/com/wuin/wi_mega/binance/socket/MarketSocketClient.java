package com.wuin.wi_mega.binance.socket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wuin.wi_mega.binance.BaseSocketClient;
import com.wuin.wi_mega.common.cache.local.DepthCacheFactory;
import com.wuin.wi_mega.common.cache.local.KlineCacheFactory;
import com.wuin.wi_mega.common.enums.KlineIntervalEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.common.util.HttpClientUtils;
import com.wuin.wi_mega.binance.bo.Kline;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MarketSocketClient extends BaseSocketClient {

    private static final String KLINE_SUFFIX = "@kline_";
    private static final String DEPTH_SUFFIX = "@depth";
    private static final String WS_BASE = "wss://fstream.binance.com/stream?streams=";
    private static final String REST_BASE = "https://fapi.binance.com";

    private final List<String> streams = new ArrayList<>();

    private final DepthCacheFactory depthCacheFactory;

    private final KlineCacheFactory klineCacheFactory;

    private volatile WebSocketConnection connection;

    private final Map<String, Long> lastUpdateIdMap = new ConcurrentHashMap<>();

    public MarketSocketClient(String socketId, KlineCacheFactory klineCacheFactory, DepthCacheFactory depthCacheFactory, List<String> symbols) {
        super(socketId);
        this.klineCacheFactory = klineCacheFactory;
        this.depthCacheFactory = depthCacheFactory;
        for (String symbol : symbols) {
            lastUpdateIdMap.put(symbol, -1L);
            streams.add(symbol + KLINE_SUFFIX + KlineIntervalEnum.MINUTE_1.getCodeStr()); //1分钟K线
            streams.add(symbol + KLINE_SUFFIX + KlineIntervalEnum.MINUTE_5.getCodeStr()); //5分钟K线
            streams.add(symbol + DEPTH_SUFFIX); //订单薄
        }
    }

    @Override
    protected Long restartIntervalMinutes() {
        return 1200L; //默认20小时之后重建连接(BA 24小时会强制断开连接)
    }

    @Override
    protected void startInner() {
        String url = WS_BASE + String.join("/", streams).toLowerCase();
        try {
            connection = new WebSocketConnection(new URI(url), "kline_depth", this::handlerMessage);
            connection.connectBlocking();
            log.info("市场数据已连接: {}", url);
        } catch (Exception e) {
            log.error("市场数据已连接", e);
        }
    }

    @Override
    protected void restartInner() {
        this.close();
        this.startInner();
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public Boolean isRunning() {
        return null != connection && connection.getRunning();
    }

    @Override
    protected void handlerMessage(String message) {
        JSONObject mainJSON = JSON.parseObject(message);
        if (!mainJSON.containsKey("stream")) {
            log.warn("handlerMessage -> 没有对应的stream结构, message={}", message);
            return;
        }
        if (!mainJSON.containsKey("data")) {
            log.warn("handlerMessage -> 没有对应的data结构, message={}", message);
            return;
        }
        String stream = mainJSON.getString("stream");
        if (stream.contains(KLINE_SUFFIX)) {
            log.info("k线数据来了:{}", message);
            this.handleKlineMessage(mainJSON.getJSONObject("data"));
        } else if (stream.endsWith(DEPTH_SUFFIX)) {
            log.info("深度数据来了:{}", message);
            this.handleDepthMessage(mainJSON.getJSONObject("data"));
        }
    }

    private void handleDepthMessage(JSONObject message) {
        try {
            String symbol = message.getString("s");
            if (!this.applyDiff(symbol, message)) {
                this.rebuildByFApi(symbol);
            }
        } catch (Exception e) {
            log.error("处理深度消息异常, raw={}", message, e);
        }
    }

    private void handleKlineMessage(JSONObject message) {
        try {
            if (message.containsKey("k")) {
                JSONObject k = message.getJSONObject("k");
                if (k != null) {
                    Kline kline = new Kline(k);
                    //更新K线
                    klineCacheFactory.forceAdd(SymbolEnum.valueOf(kline.getSymbol()), kline);
                    //更新最新价格
                    klineCacheFactory.updateCurPrice(SymbolEnum.valueOf(kline.getSymbol()), kline);
                } else {
                    log.warn("handleKlineMessage -> k字段为空，K线推送原始: {}", message);
                }
            } else {
                log.warn("handleKlineMessage -> 没有k字段，原始数据为: {}", message);
            }
        } catch (Exception e) {
            log.error("handleKlineMessage -> 处理K线消息异常, raw={}", message, e);
        }
    }

    /**
     * 按 Binance 文档流程加载 1000 档深度快照。
     */
    private void rebuildByFApi(String symbol) throws IOException, InterruptedException {
        String url = REST_BASE + "/fapi/v1/depth?symbol=" + symbol + "&limit=1000";
        String resp = HttpClientUtils.get(url);
        JSONObject snapshot = JSON.parseObject(resp);
        this.rebuildOrderBook(symbol, snapshot);
    }

    private void rebuildOrderBook(String symbol, JSONObject snapshot) {
        depthCacheFactory.clear(SymbolEnum.valueOf(symbol));
        depthCacheFactory.applyIncrement(SymbolEnum.valueOf(symbol), snapshot.getJSONArray("bids"), snapshot.getJSONArray("asks"));
        long lastUpdateId = snapshot.getLongValue("lastUpdateId");
        lastUpdateIdMap.put(symbol, lastUpdateId);
        log.info("rebuildOrderBook -> 订单簿快照重建完成，lastUpdateId={}", lastUpdateId);
    }

    /**
     * 应用增量深度事件，如果未连续，则需要重建
     */
    private Boolean applyDiff(String symbol, JSONObject diff) {
        long firstId = diff.getLongValue("U");
        long finalId = diff.getLongValue("u");
        Long prevId = diff.getLong("pu");
        long lastUpdateId = lastUpdateIdMap.get(symbol);
        if (finalId <= lastUpdateId) {
            log.warn("applyDiff -> old data, ignore");
            return true;
        }
        if (firstId > lastUpdateId + 1 && (prevId == null || prevId != lastUpdateId)) {
            log.warn("applyDiff -> 深度事件缺口，期望起始 <= {}，实际 {}, pu={}, 需要重新获取快照", lastUpdateId + 1, firstId, prevId);
            return false;
        }
        depthCacheFactory.applyIncrement(SymbolEnum.valueOf(symbol), diff.getJSONArray("b"), diff.getJSONArray("a"));
        lastUpdateId = finalId;
        lastUpdateIdMap.put(symbol, lastUpdateId);
        return true;
    }

}

