package com.wuin.wi_mega.job;

import com.wuin.wi_mega.service.StrategyExecutionLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 执行日志清理定时任务
 * 定期清理过期的策略执行日志，避免数据库膨胀
 */
@Component
@Slf4j
public class ExecutionLogCleanJob {

    @Autowired
    private StrategyExecutionLogService executionLogService;

    /**
     * 日志保留天数，默认30天
     */
    @Value("${strategy.execution-log.retain-days:30}")
    private int retainDays;

    /**
     * 每天凌晨3点执行日志清理
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredLogs() {
        log.info("ExecutionLogCleanJob -> 开 retainDays={}", retainDays);
        try {
            int count = executionLogService.cleanExpiredLogs(retainDays);
            log.info("ExecutionLogCleanJob -> 清理完成, 删除日志数量={}", count);
        } catch (Exception e) {
            log.error("ExecutionLogCleanJob -> 清理日志失败", e);
        }
    }
}

