package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.repository.AppStrategyExecutionLogRepository;
import com.wuin.wi_mega.repository.domain.AppStrategyExecutionLogDO;
import com.wuin.wi_mega.repository.mapper.AppStrategyExecutionLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 策略执行日志Repository实现
 */
@Repository
@Slf4j
public class AppStrategyExecutionLogRepositoryImpl
        extends ServiceImpl<AppStrategyExecutionLogMapper, AppStrategyExecutionLogDO>
        implements AppStrategyExecutionLogRepository {

    @Override
    public IPage<AppStrategyExecutionLogDO> pageList(
            Long userId,
            Long accountId,
            Long sessionId,
            Long strategyInstanceId,
            Integer logType,
            String logCategory,
            String logLevel,
            String symbol,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int page,
            int pageSize) {

        LambdaQueryWrapper<AppStrategyExecutionLogDO> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(AppStrategyExecutionLogDO::getUserId, userId);
        }
        if (accountId != null) {
            wrapper.eq(AppStrategyExecutionLogDO::getAccountId, accountId);
        }
        if (sessionId != null) {
            wrapper.eq(AppStrategyExecutionLogDO::getSessionId, sessionId);
        }
        if (strategyInstanceId != null) {
            wrapper.eq(AppStrategyExecutionLogDO::getStrategyInstanceId, strategyInstanceId);
        }
        if (logType != null) {
            wrapper.eq(AppStrategyExecutionLogDO::getLogType, logType);
        }
        if (logCategory != null && !logCategory.isEmpty()) {
            wrapper.eq(AppStrategyExecutionLogDO::getLogCategory, logCategory);
        }
        if (logLevel != null && !logLevel.isEmpty()) {
            wrapper.eq(AppStrategyExecutionLogDO::getLogLevel, logLevel);
        }
        if (symbol != null && !symbol.isEmpty()) {
            wrapper.eq(AppStrategyExecutionLogDO::getSymbol, symbol);
        }
        if (startTime != null) {
            wrapper.ge(AppStrategyExecutionLogDO::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AppStrategyExecutionLogDO::getCreateTime, endTime);
        }

        wrapper.orderByDesc(AppStrategyExecutionLogDO::getId);

        Page<AppStrategyExecutionLogDO> pageParam = new Page<>(page, pageSize);
        return page(pageParam, wrapper);
    }

    @Override
    public List<AppStrategyExecutionLogDO> listBySessionId(Long sessionId) {
        if (sessionId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AppStrategyExecutionLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppStrategyExecutionLogDO::getSessionId, sessionId);
        wrapper.orderByAsc(AppStrategyExecutionLogDO::getId);
        return list(wrapper);
    }

    @Override
    public List<AppStrategyExecutionLogDO> listRecentByAccountId(Long accountId, int limit) {
        if (accountId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AppStrategyExecutionLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppStrategyExecutionLogDO::getAccountId, accountId);
        wrapper.orderByDesc(AppStrategyExecutionLogDO::getId);
        wrapper.last("limit " + limit);
        return list(wrapper);
    }

    @Override
    public List<AppStrategyExecutionLogDO> listByStrategyInstanceId(Long strategyInstanceId, int limit) {
        if (strategyInstanceId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<AppStrategyExecutionLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppStrategyExecutionLogDO::getStrategyInstanceId, strategyInstanceId);
        wrapper.orderByDesc(AppStrategyExecutionLogDO::getId);
        wrapper.last("limit " + limit);
        return list(wrapper);
    }

    @Override
    public boolean batchSave(List<AppStrategyExecutionLogDO> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return true;
        }
        try {
            return saveBatch(logs, 100);
        } catch (Exception e) {
            log.error("batchSave -> 批量保存日志失败, size={}", logs.size(), e);
            return false;
        }
    }

    @Override
    public int cleanLogsBeforeTime(LocalDateTime beforeTime) {
        if (beforeTime == null) {
            return 0;
        }
        try {
            return baseMapper.cleanLogsBeforeTime(beforeTime);
        } catch (Exception e) {
            log.error("cleanLogsBeforeTime -> 清理日志失败, beforeTime={}", beforeTime, e);
            return 0;
        }
    }

    @Override
    public long countErrorLogs(Long accountId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AppStrategyExecutionLogDO> wrapper = new LambdaQueryWrapper<>();
        if (accountId != null) {
            wrapper.eq(AppStrategyExecutionLogDO::getAccountId, accountId);
        }
        wrapper.eq(AppStrategyExecutionLogDO::getLogLevel, "ERROR");
        if (startTime != null) {
            wrapper.ge(AppStrategyExecutionLogDO::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AppStrategyExecutionLogDO::getCreateTime, endTime);
        }
        return count(wrapper);
    }
}

