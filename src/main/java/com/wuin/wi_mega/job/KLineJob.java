package com.wuin.wi_mega.job;

import com.wuin.wi_mega.binance.market.KlineBackFillService;
import com.wuin.wi_mega.common.constants.AppConstant;
import com.wuin.wi_mega.common.enums.KlineIntervalEnum;
import com.wuin.wi_mega.service.AppConfigService;
import com.wuin.wi_mega.common.util.DateUtils;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class KLineJob implements InitializingBean {

    @Autowired
    private KlineBackFillService klineBackFillService;

    @Autowired
    private AppConfigService appConfigService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 每分钟的第5秒进行前面的5条K线的纠正
     */
    @Scheduled(cron = "5 * * * * ?")
    @Async("taskExecutor")
    public void doJob() {
        ThreadContext.put("traceId", SimpleSnowflake.nextId() + "");
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            this.doSync();
        } catch (Exception e) {
            log.error("StrategyJob doJob error", e);
        } finally {
            running.set(false);
        }
    }

    public void doSync() {
        List<String> symbols = appConfigService.getList(AppConstant.CONFIG_SUPPORT_SYMBOL, String.class);
        if (CollectionUtils.isEmpty(symbols)) {
            return;
        }
        for (String symbol : symbols) {
            LocalDateTime now = LocalDateTime.now();
            long start = DateUtils.getMinuteStartMillis(now.minusMinutes(5)); // 最近5分钟
            long end = DateUtils.getMinuteStartMillis(now) - 1; //不更新当前分钟
            try {
                klineBackFillService.fetchAndCache(symbol, KlineIntervalEnum.MINUTE_1, start, end, 3); //获取最近3分钟 1分钟唯独的K线数据
                if (now.getMinute() % 5 == 0) {
                    start = DateUtils.getMinuteStartMillis(now.minusMinutes(10)); //最近10分钟
                    klineBackFillService.fetchAndCache(symbol, KlineIntervalEnum.MINUTE_5, start, end, 3); //获取最近30分钟 5分钟纬度的K线数据
                    log.warn("KLineJob.doJob -> 回补 5分钟K线完成，symbol={}, start={}, end={}", symbol, start, end);
                }
            } catch (Exception e) {
                log.error("KLineJob.doJob -> 回补 K 线失败 symbol=" + symbol + " start=" + start + " end=" + end, e);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> symbols = appConfigService.getList(AppConstant.CONFIG_SUPPORT_SYMBOL, String.class);
        if (CollectionUtils.isEmpty(symbols)) {
            return;
        }
        for (String symbol : symbols) {
            LocalDateTime now = LocalDateTime.now();
            long start = DateUtils.getMinuteStartMillis(now.minusMinutes(90)); //最近90分钟
            long end = DateUtils.getMinuteStartMillis(now) - 1; //不更新当前分钟
            try {
                klineBackFillService.fetchAndCache(symbol, KlineIntervalEnum.MINUTE_1, start, end, 99); //获取最近30分钟 1分钟唯独的K线数据
                klineBackFillService.fetchAndCache(symbol, KlineIntervalEnum.MINUTE_5, start, end, 24); //获取最近30分钟 5分钟纬度的K线数据
            } catch (Exception e) {
                log.error("KLineJob.doJob -> 回补 K 线失败 symbol=" + symbol + " start=" + start + " end=" + end, e);
            }
        }
    }

}
