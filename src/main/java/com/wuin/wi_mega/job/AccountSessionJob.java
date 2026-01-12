package com.wuin.wi_mega.job;

import com.wuin.wi_mega.common.util.SimpleSnowflake;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.service.AppAccountService;
import com.wuin.wi_mega.service.AppAccountSessionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class AccountSessionJob {

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Autowired
    private AppAccountSessionService appAccountSessionService;

    @Autowired
    private AppAccountService appAccountService;

    /**
     * 每5秒进行一次心跳
     */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    @Async("taskExecutor")
    public void doJob() {
        ThreadContext.put("traceId", SimpleSnowflake.nextId() + "");
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            this.do4FinalSync();
        } catch (Exception e) {
            log.error("AccountSessionJob doJob error", e);
        } finally {
            running.set(false);
        }
    }

    public void do4FinalSync() {
        List<AppAccountSessionDO> sessionList = appAccountSessionService.listNeedSync();
        if (CollectionUtils.isEmpty(sessionList)) {
            return;
        }
        for (AppAccountSessionDO sessionDO : sessionList) {
            try {
                //同步当前会话盈亏信息
                appAccountSessionService.doFinalSync(sessionDO);
                //同步净值数据
                appAccountService.sync(sessionDO.getAccountId());
            } catch (Throwable e) {
                log.error("AccountSessionJob do4FinalSync error, sessionId=" + sessionDO.getId(), e);
            }
        }
    }
}
