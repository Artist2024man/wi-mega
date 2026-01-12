package com.wuin.wi_mega.job;

import com.wuin.wi_mega.binance.BinanceWebSocketRunner;
import com.wuin.wi_mega.binance.socket.UserDataSocketClient;
import com.wuin.wi_mega.common.exception.APIRuntimeException;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class AccountSocketJob {

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Autowired
    private BinanceWebSocketRunner binanceWebSocketRunner;

    /**
     * 每分钟进行一次心跳
     */
    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
    @Async("taskExecutor")
    public void doJob() {
        ThreadContext.put("traceId", SimpleSnowflake.nextId() + "");
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            this.doKeepalive();
        } catch (Exception e) {
            log.error("UserSocketKeepaliveJob doJob error", e);
        } finally {
            running.set(false);
        }

    }

    public void doKeepalive() {
        if (binanceWebSocketRunner.getClientMap().isEmpty()) {
            log.warn("doKeepalive -> socket client is empty, ignore");
            return;
        }
        binanceWebSocketRunner.getClientMap().forEach((skId, client) -> {
            // ========== 修改：增加try-catch，单个账户失败不影响其他账户 ==========
            try {
                if (client instanceof UserDataSocketClient) {
                    ((UserDataSocketClient) client).tryRefreshKey();
                }
            } catch (APIRuntimeException e) {
                // API Key无效等业务异常，记录日志但不中断其他账户
                log.warn("doKeepalive -> 账户心跳失败, skId={}, error={}", skId, e.getMessage());
            } catch (Exception e) {
                // 其他未知异常
                log.error("doKeepalive -> 账户心跳异常, skId={}", skId, e);
            }
            // ========== 修改结束 ==========
        });
    }
}
