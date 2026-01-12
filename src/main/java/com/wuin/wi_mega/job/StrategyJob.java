package com.wuin.wi_mega.job;


import com.wuin.wi_mega.common.enums.StrategyStatusEnum;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.service.AppAccountService;
import com.wuin.wi_mega.service.AppAccountSessionService;
import com.wuin.wi_mega.strategy.StrategyFactory;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StrategyJob {

    @Autowired
    private StrategyFactory strategyFactory;

    @Autowired
    private AppAccountService appAccountService;

    @Autowired
    private AppAccountSessionService appAccountSessionService;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final Semaphore semaphore = new Semaphore(10); //最多10个并发

    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 每分钟的第27秒开始处理
     */
    @Scheduled(cron = "15,25,35,45 * * * * ?")
    @Async("taskExecutor")
    public void doJob() {
        ThreadContext.put("traceId", SimpleSnowflake.nextId() + "");
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            doTaskRun();
        } catch (Exception e) {
            log.error("StrategyJob doJob error", e);
        } finally {
            running.set(false);
        }
    }

    private void doTaskRun() {

        long startTime = System.currentTimeMillis();

        List<AppAccountDO> accountDOList = appAccountService.listByStatus(StrategyStatusEnum.RUNNING);
        if (CollectionUtils.isEmpty(accountDOList)) {
            return;
        }

        List<AppAccountSessionDO> sessionList = appAccountSessionService.listOpeningByAccountIds(accountDOList.stream().map(AppAccountDO::getId).collect(Collectors.toSet()));
        Map<Long, AppAccountSessionDO> runningSessionMap = sessionList.stream().collect(Collectors.toMap(AppAccountSessionDO::getAccountId, session -> session, (a, b) -> b));

        accountDOList = accountDOList.stream().filter(accountDO -> !runningSessionMap.containsKey(accountDO.getId())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(accountDOList)) {
            return;
        }

        CountDownLatch latch = new CountDownLatch(accountDOList.size());

        for (AppAccountDO accountDO : accountDOList) {
            try {
                semaphore.acquireUninterruptibly();
                executor.execute(() -> {
                    try {
                        strategyFactory.execute(accountDO);
                    } catch (Throwable t) {
                        log.error("StrategyJob doJob error", t);
                    } finally {
                        semaphore.release();
                        latch.countDown();
                    }
                });
            } catch (Throwable t) {
                log.error("StrategyJob doTaskRun vir thread error, id=" + accountDO.getId(), t);
                semaphore.release();
                latch.countDown();
            }
        }

        try {
            boolean success = latch.await(10, TimeUnit.SECONDS);
            log.warn("StrategyJob doJob finish, success={}, cost={}, accountSize={}", success, System.currentTimeMillis() - startTime, accountDOList.size());
        } catch (InterruptedException e) {
            log.error("StrategyJob doJob error, wait fail", e);
        }
    }
}
