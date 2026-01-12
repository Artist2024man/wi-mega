package com.wuin.wi_mega.binance.socket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wuin.wi_mega.binance.BaseSocketClient;
import com.wuin.wi_mega.binance.bo.UserSocketDataEvent;
import com.wuin.wi_mega.queue.impl.UserDataQueue;
import com.wuin.wi_mega.common.util.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
public class UserDataSocketClient extends BaseSocketClient {

    private static final Long LISTEN_KEY_EXPIRE_MINUTES = 45L;

    private static final String REST_BASE = "https://fapi.binance.com";
    private static final String WS_BASE = "wss://fstream.binance.com/ws/";
    private final String accountId;

    private final String apiKey;

    private String listenKey;

    private WebSocketConnection connection;

    private LocalDateTime keepAliveTimeout;

    private final UserDataQueue userDataQueue;

    public UserDataSocketClient(String accountId, String apiKey, UserDataQueue userDataQueue) {
        super(accountId);
        this.accountId = accountId;
        this.apiKey = apiKey;
        this.userDataQueue = userDataQueue;
    }

    @Override
    protected Long restartIntervalMinutes() {
        return 1200L; //默认 20小时后重新建立连接
    }

    @Override
    protected void startInner() {
        try {
            this.listenKey = createListenKey(apiKey);
            String url = WS_BASE + listenKey;
            this.connection = new WebSocketConnection(new URI(url), "user-data-" + this.accountId, this::handlerMessage);
            connection.connectBlocking();
            log.info("用户-{}, 数据已链接: {}", this.accountId, url);
        } catch (Exception e) {
            log.error("用户-" + this.accountId + ", 连接失败, listenKey=" + this.listenKey, e);
            throw new RuntimeException("账户连接异常-" + this.accountId);
        }
    }

    @Override
    protected void restartInner() {
        this.close();
        this.startInner();
    }

    @Override
    public void close() {
        if (null != connection) {
            connection.close();
        }
    }

    @Override
    public Boolean isRunning() {
        return null != connection && connection.getRunning();
    }

    @Override
    protected void handlerMessage(String message) {
        log.warn("获取到用户数据:{}", message);
        JSONObject jsonObject = JSON.parseObject(message);
        String eventType = jsonObject.getString("e");

        UserSocketDataEvent event = new UserSocketDataEvent();
        event.setEvent(eventType);

        switch (eventType) {
            case "TRADE_LITE" -> {
                event.setClientOrderId(jsonObject.getString("c"));
                event.setOrderId(jsonObject.getLong("i"));
            }
            case "ORDER_TRADE_UPDATE" -> {
                JSONObject orderJSON = jsonObject.getJSONObject("o");
                event.setClientOrderId(orderJSON.getString("c"));
                event.setOrderId(orderJSON.getLong("i"));
                event.setOrderType(orderJSON.getString("o"));
            }
            case "ALGO_UPDATE" -> {
                JSONObject orderJSON = jsonObject.getJSONObject("o");
                event.setClientOrderId(orderJSON.getString("caid"));
                event.setOrderId(orderJSON.getLong("aid"));
                event.setOrderType(orderJSON.getString("o"));
            }
        }
        userDataQueue.offer(event);
    }

    private String createListenKey(String apiKey) {
        try {
            String url = REST_BASE + "/fapi/v1/listenKey";
            String resp = HttpClientUtils.post(url, null, Map.of("X-MBX-APIKEY", apiKey));
            JSONObject json = JSON.parseObject(resp);
            return json.getString("listenKey");
        } catch (Throwable e) {
            log.error("createListenKey exception, apiKey=" + apiKey, e);
            throw new RuntimeException("createListenKey fail");
        }
    }

    public void tryRefreshKey() {
        if (null != this.keepAliveTimeout && LocalDateTime.now().isAfter(keepAliveTimeout)) {
            this.keepAlive();
        }
    }

    private void keepAlive() {
        try {
            String url = REST_BASE + "/fapi/v1/listenKey?listenKey=" + this.listenKey;
            String resp = HttpClientUtils.put(url, null, Map.of("X-MBX-APIKEY", apiKey));
            log.info("[user-stream:{}] keepalive ok, listenKey={}, resp={}", this.accountId, this.listenKey, resp);
            this.keepAliveTimeout = LocalDateTime.now().plusMinutes(LISTEN_KEY_EXPIRE_MINUTES);
        } catch (Exception e) {
            log.error("[user-stream:" + this.accountId + "] keepalive exception, listenKey=" + listenKey, e);
        }
    }

}
