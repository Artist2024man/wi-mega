package com.wuin.wi_mega.binance;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用启动时自动连接 Binance 行情流。
 */
@Slf4j
@Component
public class BinanceWebSocketRunner {

    @Getter
    private final Map<String, BaseSocketClient> clientMap = new ConcurrentHashMap<>();
    private final Thread thread;

    public BinanceWebSocketRunner() {
        thread = new Thread(() -> {
            log.info("SocketRunner -> 心跳守护进程启动...");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!clientMap.isEmpty()) {
                        clientMap.forEach((skId, client) -> {
                            if (!isRunning(client)) {
                                tryStart(client); //尝试启动
                            } else {
                                tryReStart(client); //尝试重启
                            }
                        });
                    }
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("守护进程执行异常", e);
                }
            }
        });
        thread.start();
    }

    public void join(BaseSocketClient socketClient) {
        if (null == socketClient) {
            throw new IllegalArgumentException("socketClient is null");
        }
        if (clientMap.containsKey(socketClient.getSocketId())) {
            log.warn("join -> 系统中已经包含对应的客户端，停用之前的client，skId={}", socketClient.getSocketId());
            this.leave(socketClient.getSocketId());
        }
        socketClient.start();
        clientMap.put(socketClient.getSocketId(), socketClient);
    }

    public void leave(String socketId) {
        BaseSocketClient client = clientMap.get(socketId);
        if (null == client) {
            log.warn("socketId:{} not exist", socketId);
            return;
        }
        client.close();
        clientMap.remove(socketId);
    }

    @PreDestroy
    public void onShutdown() {
        log.info("关闭 WebSocket 连接...");
        if (!clientMap.isEmpty()) {
            clientMap.forEach((skId, client) -> {
                try {
                    client.close();
                } catch (Throwable ignored) {
                }
            });
        }
    }

    private void tryStart(BaseSocketClient client) {
        try {
            if (null == client) {
                log.warn("tryStart -> client is null");
                return;
            } else {
                client.close();
            }
            client.start();
        } catch (Throwable t) {
            log.error("tryStart -> failed" + client, t);
        }
    }

    public void tryReStart(BaseSocketClient client) {
        try {
            if (null == client) {
                log.warn("tryReStart -> client is null");
                return;
            }
            client.tryRestart();
        } catch (Throwable t) {
            log.error("tryReStart -> failed" + (null == client ? "null" : client.getSocketId()), t);
        }
    }

    private boolean isRunning(BaseSocketClient client) {
        if (null == client) {
            return false;
        }
        return client.isRunning();
    }


}

