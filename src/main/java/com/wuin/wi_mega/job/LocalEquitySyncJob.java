package com.wuin.wi_mega.job;

import com.wuin.wi_mega.service.AccountEquityService;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class LocalEquitySyncJob {

    @Autowired
    private AccountEquityService accountEquityService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 1分钟一次
     */
    @Scheduled(cron = "59 * * * * *")
    @Async("taskExecutor")
    public void doJob() {
        ThreadContext.put("traceId", SimpleSnowflake.nextId() + "");
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            accountEquityService.sync();
        } catch (Exception e) {
            log.error("LocalEquitySyncJob doJob error", e);
            running.set(false);
            throw new RuntimeException("LocalEquitySyncJob doJob error", e);
        } finally {
            running.set(false);
        }
    }

}
