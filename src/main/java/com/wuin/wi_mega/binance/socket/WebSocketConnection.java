package com.wuin.wi_mega.binance.socket;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 简单的 WebSocket 连接封装，负责基础的生命周期日志与消息分发。
 */
@Slf4j
public class WebSocketConnection extends WebSocketClient {

    private final String name;
    private final Consumer<String> onMessageHandler;

    @Getter
    private Boolean running = true;

    public WebSocketConnection(URI serverUri, String name, Consumer<String> onMessageHandler) {
        super(serverUri);
        this.name = name;
        this.onMessageHandler = onMessageHandler;
        this.setConnectionLostTimeout(0);
    }

    public WebSocketConnection(URI serverUri, Map<String, String> httpHeaders, String name, Consumer<String> onMessageHandler) {
        super(serverUri, httpHeaders);
        this.name = name;
        this.onMessageHandler = onMessageHandler;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        running = true;
        log.info("[{}] WebSocket 已连接，状态码: {}", name, handshakedata.getHttpStatus());
    }

    @Override
    public void onMessage(String message) {
        if (onMessageHandler != null) {
            onMessageHandler.accept(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        running = false;
        log.warn("[{}] WebSocket 关闭，code={}, reason={}, remote={}", name, code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        running = false;
        log.error("[{}] WebSocket 异常", name, ex);
    }
}

