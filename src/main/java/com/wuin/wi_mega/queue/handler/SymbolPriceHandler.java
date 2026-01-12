package com.wuin.wi_mega.queue.handler;

import com.wuin.wi_mega.common.cache.local.StrategySignalCache;
import com.wuin.wi_mega.common.enums.SessionStatusEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.model.bo.signal.StrategyStartSignalBO;
import com.wuin.wi_mega.queue.impl.SymbolPriceQueue;
import com.wuin.wi_mega.repository.AppAccountSessionRepository;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.strategy.StrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Component
@Slf4j
public class SymbolPriceHandler implements InitializingBean {

    @Autowired
    private SymbolPriceQueue symbolPriceQueue;

    @Autowired
    private StrategySignalCache strategySignalCache;

    @Autowired
    private StrategyFactory strategyFactory;

    @Autowired
    private AppAccountSessionRepository appAccountSessionRepository;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final Semaphore semaphore = new Semaphore(20); //最多20个并发

    private Thread thread;

    private void handle(String symbol) {
        SymbolEnum symbolEnum = SymbolEnum.valueOf(symbol);
        this.handlerRunning(symbolEnum);
        this.handlerAccountSignal(symbolEnum);
    }

    private void handlerAccountSignal(SymbolEnum symbolEnum) {
        try {
            Set<StrategyStartSignalBO> signalBOSet = strategySignalCache.get(symbolEnum);
            if (CollectionUtils.isEmpty(signalBOSet)) {
                return;
            }

            CountDownLatch latch = new CountDownLatch(signalBOSet.size());
            for (StrategyStartSignalBO signalBO : signalBOSet) {
                boolean acquired = false;
                try {
                    semaphore.acquireUninterruptibly();
                    acquired = true;
                    executor.execute(() -> {
                        try {
                            strategyFactory.handlerStartSignal(signalBO);
                        } catch (Throwable t) {
                            log.error("handlerRunning -> exception, accountId={}", signalBO.getAccountId(), t);
                        } finally {
                            //开仓后强制删除缓存
                            strategySignalCache.remove(signalBO);
                            semaphore.release();
                            latch.countDown();
                        }
                    });
                } catch (Throwable t) {
                    if (acquired) {
                        semaphore.release();
                    }
                    latch.countDown();
                }
            }

            try {
                boolean success = latch.await(10, TimeUnit.SECONDS);
//                log.warn("handlerAccountSignal finish, success={}, cost={}, size={}", success, System.currentTimeMillis() - startTime, signalBOSet.size());
            } catch (InterruptedException e) {
                log.error("handlerAccountSignal error, wait fail", e);
            }

        } catch (Throwable t) {
            log.error("handlerAccountSignal -> exception", t);
        }
    }

    private void handlerRunning(SymbolEnum symbolEnum) {
        try {
            List<Long> sessionIds = appAccountSessionRepository.listIdBySymbol(symbolEnum, SessionStatusEnum.profiting());
            if (CollectionUtils.isEmpty(sessionIds)) {
                return;
            }

            CountDownLatch latch = new CountDownLatch(sessionIds.size());
            for (Long sessionId : sessionIds) {
                boolean acquired = false;
                try {
                    semaphore.acquireUninterruptibly();
                    acquired = true;
                    executor.execute(() -> {
                        try {
                            AppAccountSessionDO sessionDO = appAccountSessionRepository.getById(sessionId);
                            if (null == sessionDO) {
                                log.warn("handlerRunning -> invalid sessionId {}", sessionId);
                                return;
                            }
                            strategyFactory.handlerRunningSignal(sessionDO);
                        } catch (Throwable t) {
                            log.error("handlerRunning -> exception, sessionId=" + sessionId, t);
                        } finally {
                            semaphore.release();
                            latch.countDown();
                        }
                    });
                } catch (Throwable t) {
                    if (acquired) {
                        semaphore.release();
                    }
                    latch.countDown();
                }
            }

            try {
                latch.await(10, TimeUnit.SECONDS);
//                log.warn("handlerRunning finish, success={}, cost={}, size={}", success, System.currentTimeMillis() - startTime, sessionIds.size());
            } catch (InterruptedException e) {
                log.error("handlerRunning error, wait fail", e);
            }

        } catch (Throwable t) {
            log.error("handlerRunning -> exception", t);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        thread = new Thread(() -> {
            log.info("BinanceUserDataHandler -> 用户数据处理线程...");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String symbol = symbolPriceQueue.take();
                    this.handle(symbol);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("BinanceUserDataHandler -> 用户数据处理异常", e);
                }
            }
        });
        thread.start();
    }
}
