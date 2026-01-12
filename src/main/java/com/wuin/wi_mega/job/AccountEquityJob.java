package com.wuin.wi_mega.job;

import com.wuin.wi_mega.service.AppAccountService;
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
public class AccountEquityJob {

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Autowired
    private AppAccountService appAccountService;

    /**
     * 每5秒尝试同步一次净值数据
     */
    @Scheduled(cron = "0/5 * * * * ?")
    @Async("taskExecutor")
    public void doJob() {
        ThreadContext.put("traceId", SimpleSnowflake.nextId() + "");
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            appAccountService.syncEquity();
        } catch (Exception e) {
            log.error("AccountEquityJob doJob error", e);
        } finally {
            running.set(false);
        }

    }
}
