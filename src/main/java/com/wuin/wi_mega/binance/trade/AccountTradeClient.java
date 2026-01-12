package com.wuin.wi_mega.binance.trade;

import com.alibaba.fastjson2.JSONObject;
import com.wuin.wi_mega.common.cache.local.BinanceApiWeightCache;
import com.wuin.wi_mega.common.util.HmacUtil;
import com.wuin.wi_mega.common.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

@Slf4j
public class AccountTradeClient {

    private static final String REST_BASE = "https://fapi.binance.com";
    // 统一账户/投资组合保证金接口基址
    private static final long DEFAULT_RECV_WINDOW = 5_000;

    private static final BinanceApiWeightCache apiWeightCache = new BinanceApiWeightCache(2000);

    private final String apiKey;
    private final String secretKey;

    public AccountTradeClient(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public String cancelOrder(String symbol, Long orderId, String clientOrderId) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        if (orderId != null) params.put("orderId", String.valueOf(orderId));
        if (clientOrderId != null) params.put("origClientOrderId", clientOrderId);
        return signedDelete("/fapi/v1/order", params, 1);
    }

    public String cancelAllAlgoOrders(String symbol) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        return signedDelete("/fapi/v1/algoOpenOrders", params, 1);
    }

    public String cancelAlgoOrders(String symbol, String clientAlgoId, Long algoId) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        if (null != clientAlgoId) params.put("clientAlgoId", clientAlgoId);
        if (algoId != null) params.put("algoId", String.valueOf(algoId));
        return signedDelete("/fapi/v1/algoOrder", params, 1);
    }

    public String algoOrders(String symbol) {
        Map<String, String> params = new LinkedHashMap<>();
        if (symbol != null) params.put("symbol", symbol);
        return signedGet("/fapi/v1/openAlgoOrders", params, 1);
    }

    public String queryOrder(String symbol, Long orderId, String clientOrderId) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        if (orderId != null) params.put("orderId", String.valueOf(orderId));
        if (clientOrderId != null) params.put("origClientOrderId", clientOrderId);
        return signedGet("/fapi/v1/order", params, 1);
    }

    public String queryOpenOrder(String symbol, Long orderId, String clientOrderId) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        if (orderId != null) params.put("orderId", String.valueOf(orderId));
        if (clientOrderId != null) params.put("origClientOrderId", clientOrderId);
        return signedGet("/fapi/v1/openOrder", params, 1);
    }

    public String openOrders(String symbol) {
        Map<String, String> params = new LinkedHashMap<>();
        if (symbol != null) params.put("symbol", symbol);
        return signedGet("/fapi/v1/openOrders", params, 1);
    }

    public String userTrades(String symbol, Long orderId, Long fromId, Integer limit, Long startTime, Long endTime) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        if (orderId != null) params.put("orderId", String.valueOf(orderId));
        if (fromId != null) params.put("fromId", String.valueOf(orderId));
        if (limit != null) params.put("limit", String.valueOf(limit));
        if (startTime != null) params.put("startTime", String.valueOf(startTime));
        if (endTime != null) params.put("endTime", String.valueOf(endTime));
        return signedGet("/fapi/v1/userTrades", params, 5);
    }

    public String positionRiskV3(String symbol) {
        String path = "/fapi/v3/positionRisk";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        return signedGet(path, params, 5);
    }

    public String fetchBalanceV3() {
        String path = "/fapi/v3/balance";
        return signedGet(path, Map.of(), 5);
    }

    /**
     * 账户信息 V3 (USER-DATA)
     * GET /fapi/v3/account
     */
    public String fetchAccountInfoV3() {
        String path = "/fapi/v2/account";
        return signedGet(path, Map.of(), 5);
    }

    public String changePositionMode(boolean dualSide) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("dualSidePosition", String.valueOf(dualSide));
        return signedPost("/fapi/v1/positionSide/dual", params, 1);
    }

    public String changeLeverage(String symbol, int leverage) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("leverage", String.valueOf(leverage));
        return signedPost("/fapi/v1/leverage", params, 1);
    }

    public String isDualSidePosition() {
        String path = "/fapi/v1/positionSide/dual";
        return signedGet(path, Map.of(), 30);
    }

    public String feeRate(String symbol) {
        String path = "/fapi/v1/commissionRate";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        return signedGet(path, params, 20);
    }

    /* 便捷方法 */
    public String openLong(String clientOrderId, String symbol, BigDecimal price, BigDecimal qty, String tif) {
        return placeLimit(clientOrderId, symbol, "BUY", "LONG", price, qty, tif);
    }

    public String openShort(String clientOrderId, String symbol, BigDecimal price, BigDecimal qty, String tif) {
        return placeLimit(clientOrderId, symbol, "SELL", "SHORT", price, qty, tif);
    }

    public String closeLong(String clientOrderId, String symbol, BigDecimal qty) {
        return placeMarket(clientOrderId, symbol, "SELL", "LONG", qty, true);
    }

    public String closeShort(String clientOrderId, String symbol, BigDecimal qty) {
        return placeMarket(clientOrderId, symbol, "BUY", "SHORT", qty, true);
    }

    /** 市价开多 */
    public String marketOpenLong(String clientOrderId, String symbol, BigDecimal qty) {
        return placeMarket(clientOrderId, symbol, "BUY", "LONG", qty);
    }

    /** 市价开空 */
    public String marketOpenShort(String clientOrderId, String symbol, BigDecimal qty) {
        return placeMarket(clientOrderId, symbol, "SELL", "SHORT", qty);
    }

    public String takeProfit(String symbol, String positionSide, BigDecimal stopPrice) {
        String side = closeSide(positionSide);
        return placeAlgoConditional(symbol, side, positionSide, "TAKE_PROFIT_MARKET", stopPrice, null, true, null);
    }

    public String stopLoss(String symbol, String positionSide, BigDecimal stopPrice) {
        String side = closeSide(positionSide);
        return placeAlgoConditional(symbol, side, positionSide, "STOP_MARKET", stopPrice, null, true, null);
    }

    public JSONObject closeAll(String symbol) throws Exception {
//        JSONObject risk = positionRisk(symbol);
//        JSONArray arr = risk.getJSONArray("data") != null ? risk.getJSONArray("data") : risk.getJSONArray("body");
//        if (arr == null) {
//            log.warn("closeAll -> no position data");
//            return risk;
//        }
        JSONObject result = new JSONObject();
//        for (int i = 0; i < arr.size(); i++) {
//            JSONObject pos = arr.getJSONObject(i);
//            String posSide = pos.getString("positionSide");
//            BigDecimal amt = pos.getBigDecimal("positionAmt");
//            if (amt == null || amt.compareTo(BigDecimal.ZERO) == 0) continue;
//            BigDecimal qty = amt.abs();
//            String side = closeSide(posSide);
//            String resp = placeMarket(pos.getString("symbol"), side, posSide, qty);
//            result.put(posSide + "-" + pos.getString("symbol"), resp);
//        }
        return result;
    }

    /* --------------- internal helpers --------------- */

    private Map<String, String> baseOrderParams(String symbol, String side, String positionSide) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("positionSide", positionSide);
        params.put("newOrderRespType", "RESULT");
        return params;
    }

    private String signedGet(String path, Map<String, String> params, Integer weight) {
        return signedGetWithBase(REST_BASE, path, params, weight);
    }

    private String signedGetWithBase(String base, String path, Map<String, String> params, Integer weight) {
        Map<String, String> p = withSignature(params);
        String url = base + path + "?" + toQueryString(p);
        log.warn("signedGetWithBase -> request url:{}, used.weight={}", url, apiWeightCache.use(weight));
        try {
            return HttpClientUtils.get(url, Map.of("X-MBX-APIKEY", apiKey));
        } catch (IOException | InterruptedException t) {
            log.error("signedGet -> exception, url=" + url, t);
            throw new RuntimeException("signedGet fail");
        }

    }

    private String signedPost(String path, Map<String, String> params, Integer weight) {
        Map<String, String> p = withSignature(params);
        String body = toQueryString(p);
        String url = REST_BASE + path;
        log.warn("signedPost -> request url:{}, body={}, used.weight={}", url, body, apiWeightCache.use(weight));
        try {
            return HttpClientUtils.postForm(url, body, Map.of("X-MBX-APIKEY", apiKey));
        } catch (IOException | InterruptedException t) {
            log.error("signedPost -> exception, url=" + url, t);
            throw new RuntimeException("signedPost fail");
        }
    }

    private String signedDelete(String path, Map<String, String> params, int weight) {
        Map<String, String> p = withSignature(params);
        String url = REST_BASE + path + "?" + toQueryString(p);
        log.warn("signedDelete -> request url:{}, used.weight={}", url, apiWeightCache.use(weight));
        try {
            return HttpClientUtils.delete(url, Map.of("X-MBX-APIKEY", apiKey));
        } catch (IOException | InterruptedException t) {
            log.error("signedDelete -> exception, url=" + url, t);
            throw new RuntimeException("signedDelete fail");
        }
    }

    private Map<String, String> withSignature(Map<String, String> params) {
        Map<String, String> p = new LinkedHashMap<>(params);
        p.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));
        p.put("recvWindow", String.valueOf(DEFAULT_RECV_WINDOW));
        String qs = toQueryString(p);
        String sig = HmacUtil.hmacSha256(secretKey, qs);
        p.put("signature", sig);
        return p;
    }

    private String toQueryString(Map<String, String> params) {
        StringJoiner joiner = new StringJoiner("&");
        params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> joiner.add(e.getKey() + "=" + e.getValue()));
        return joiner.toString();
    }

    private String closeSide(String positionSide) {
        return "LONG".equalsIgnoreCase(positionSide) ? "SELL" : "BUY";
    }

    private String str(BigDecimal v) {
        return v == null ? null : v.stripTrailingZeros().toPlainString();
    }

    /* 下单与撤单接口（REST） */
    private String placeLimit(String clientOrderId, String symbol, String side, String positionSide,
                                  BigDecimal price, BigDecimal quantity, String timeInForce) {
        Map<String, String> params = baseOrderParams(symbol, side, positionSide);
        params.put("type", "LIMIT");
        params.put("price", str(price));
        params.put("newClientOrderId", clientOrderId);
        params.put("quantity", str(quantity));
        params.put("timeInForce", timeInForce);
        return signedPost("/fapi/v1/order", params, 1);
    }

    private String placeMarket(String clientOrderId, String symbol, String side, String positionSide,
                                   BigDecimal quantity) {
        return this.placeMarket(clientOrderId, symbol, side, positionSide, quantity, null);
    }

    private String placeMarket(String clientOrderId, String symbol, String side, String positionSide,
                               BigDecimal quantity, Boolean reduceOnly) {
        Map<String, String> params = baseOrderParams(symbol, side, positionSide);
        params.put("type", "MARKET");
        params.put("newClientOrderId", clientOrderId);
        params.put("quantity", str(quantity));
//        if (null != reduceOnly) {
//            params.put("reduceOnly", String.valueOf(reduceOnly));
//        }
        return signedPost("/fapi/v1/order", params, 1);
    }

    /**
     * 条件单下单（止盈/止损改用 CONDITIONAL 接口）
     */
    private String placeAlgoConditional(String symbol, String side, String positionSide,
                                        String type, BigDecimal triggerPrice, BigDecimal price,
                                        boolean closePosition, BigDecimal quantity) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("algoType", "CONDITIONAL");
        params.put("symbol", symbol);
        params.put("side", side);
        params.put("positionSide", positionSide);
        params.put("type", type); // STOP / TAKE_PROFIT / STOP_MARKET / TAKE_PROFIT_MARKET ...
        if (price != null) {
            params.put("price", str(price));
        }
        if (triggerPrice != null) {
            params.put("triggerPrice", str(triggerPrice));
        }
        params.put("closePosition", String.valueOf(closePosition));
        if (!closePosition && quantity != null) {
            params.put("quantity", str(quantity));
        }
        // 默认 workingType=CONTRACT_PRICE；timeInForce 默认 GTC
        return signedPost("/fapi/v1/algoOrder", params, 1);
    }
}

