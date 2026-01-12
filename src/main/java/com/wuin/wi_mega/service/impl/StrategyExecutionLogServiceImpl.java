package com.wuin.wi_mega.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wuin.wi_mega.common.enums.ExecutionLogTypeEnum;
import com.wuin.wi_mega.common.enums.MockDataEnum;
import com.wuin.wi_mega.common.enums.TradeTypeEnum;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.vo.ExecutionLogQueryReqVO;
import com.wuin.wi_mega.model.vo.ExecutionLogVO;
import com.wuin.wi_mega.repository.AppStrategyExecutionLogRepository;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.repository.domain.AppStrategyExecutionLogDO;
import com.wuin.wi_mega.service.StrategyExecutionLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 策略执行日志服务实现
 */
@Service
@Slf4j
public class StrategyExecutionLogServiceImpl implements StrategyExecutionLogService {

    @Autowired
    private AppStrategyExecutionLogRepository executionLogRepository;

    @Override
    public PageResponseVO<ExecutionLogVO> pageList(Long userId, ExecutionLogQueryReqVO reqVO) {
        if (reqVO == null) {
            reqVO = new ExecutionLogQueryReqVO();
        }

        int page = reqVO.getPage() != null ? reqVO.getPage() : 1;
        int pageSize = reqVO.getPageSize() != null ? reqVO.getPageSize() : 20;

        IPage<AppStrategyExecutionLogDO> pageResult = executionLogRepository.pageList(
                userId,
                reqVO.getAccountId(),
                reqVO.getSessionId(),
                reqVO.getStrategyInstanceId(),
                reqVO.getLogType(),
                reqVO.getLogCategory(),
                reqVO.getLogLevel(),
                reqVO.getSymbol(),
                reqVO.getStartTime(),
                reqVO.getEndTime(),
                page,
                pageSize
        );

        List<ExecutionLogVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResponseVO<ExecutionLogVO> response = new PageResponseVO<>();
        response.setRecords(voList);
        response.setTotal(pageResult.getTotal());
        return response;
    }

    @Override
    public List<ExecutionLogVO> listBySessionId(Long sessionId) {
        List<AppStrategyExecutionLogDO> logs = executionLogRepository.listBySessionId(sessionId);
        return logs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExecutionLogVO> listRecentByAccountId(Long accountId, int limit) {
        List<AppStrategyExecutionLogDO> logs = executionLogRepository.listRecentByAccountId(accountId, limit);
        return logs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ExecutionLogVO getLogDetail(Long logId) {
        AppStrategyExecutionLogDO logDO = executionLogRepository.getById(logId);
        if (logDO == null) {
            return null;
        }
        return convertToVO(logDO);
    }

    @Override
    @Async
    public void log(AppStrategyExecutionLogDO logDO) {
        try {
            if (logDO.getId() == null) {
                logDO.setId(SimpleSnowflake.nextId());
            }
            if (logDO.getCreateTime() == null) {
                logDO.setCreateTime(LocalDateTime.now());
            }
            if (logDO.getUpdateTime() == null) {
                logDO.setUpdateTime(LocalDateTime.now());
            }
            executionLogRepository.save(logDO);
        } catch (Exception e) {
            log.error("log -> 保存执行日志失败, logDO={}", JSON.toJSONString(logDO), e);
        }
    }

    @Override
    @Async
    public void logSignal(AppAccountDO accountDO, ExecutionLogTypeEnum logType,
                          String title, String content, BigDecimal currentPrice, String positionSide) {
        try {
            AppStrategyExecutionLogDO logDO = buildBasicLog(accountDO, logType);
            logDO.setTitle(title);
            logDO.setContent(content);
            logDO.setCurrentPrice(currentPrice);
            logDO.setPositionSide(positionSide);
            logDO.setLogLevel("INFO");
            logDO.setResult("SUCCESS");
            log(logDO);
        } catch (Exception e) {
            log.error("logSignal -> 记录信号日志失败", e);
        }
    }

    @Override
    @Async
    public void logSession(AppAccountSessionDO sessionDO, ExecutionLogTypeEnum logType,
                           String title, String content, String result) {
        try {
            AppStrategyExecutionLogDO logDO = buildSessionLog(sessionDO, logType);
            logDO.setTitle(title);
            logDO.setContent(content);
            logDO.setResult(result);
            logDO.setLogLevel("SUCCESS".equals(result) ? "INFO" : "WARN");
            log(logDO);
        } catch (Exception e) {
            log.error("logSession -> 记录会话日志失败", e);
        }
    }

    @Override
    @Async
    public void logSessionWithPrice(AppAccountSessionDO sessionDO, ExecutionLogTypeEnum logType,
                                    String title, String content,
                                    BigDecimal currentPrice, BigDecimal targetPrice, String result) {
        try {
            AppStrategyExecutionLogDO logDO = buildSessionLog(sessionDO, logType);
            logDO.setTitle(title);
            logDO.setContent(content);
            logDO.setCurrentPrice(currentPrice);
            logDO.setTargetPrice(targetPrice);
            logDO.setResult(result);
            logDO.setLogLevel("SUCCESS".equals(result) ? "INFO" : "WARN");
            log(logDO);
        } catch (Exception e) {
            log.error("logSessionWithPrice -> 记录会话日志失败", e);
        }
    }

    @Override
    @Async
    public void logOrder(AppAccountDO accountDO, Long sessionId, Long orderId,
                         ExecutionLogTypeEnum logType, String title, String content,
                         BigDecimal price, BigDecimal qty, String result) {
        try {
            AppStrategyExecutionLogDO logDO = buildBasicLog(accountDO, logType);
            logDO.setSessionId(sessionId);
            logDO.setOrderId(orderId);
            logDO.setTitle(title);
            logDO.setContent(content);
            logDO.setCurrentPrice(price);
            logDO.setHoldQty(qty);
            logDO.setResult(result);
            logDO.setLogLevel("SUCCESS".equals(result) ? "INFO" : "WARN");
            log(logDO);
        } catch (Exception e) {
            log.error("logOrder -> 记录订单日志失败", e);
        }
    }

    @Override
    @Async
    public void logError(AppAccountDO accountDO, Long sessionId, String title, String errorMsg) {
        try {
            AppStrategyExecutionLogDO logDO = buildBasicLog(accountDO, ExecutionLogTypeEnum.SYSTEM_ERROR);
            logDO.setSessionId(sessionId);
            logDO.setTitle(title);
            logDO.setErrorMsg(errorMsg);
            logDO.setLogLevel("ERROR");
            logDO.setResult("FAILED");
            log(logDO);
        } catch (Exception e) {
            log.error("logError -> 记录错误日志失败", e);
        }
    }

    @Override
    @Async
    public void batchLog(List<AppStrategyExecutionLogDO> logs) {
        try {
            LocalDateTime now = LocalDateTime.now();
            for (AppStrategyExecutionLogDO logDO : logs) {
                if (logDO.getId() == null) {
                    logDO.setId(SimpleSnowflake.nextId());
                }
                if (logDO.getCreateTime() == null) {
                    logDO.setCreateTime(now);
                }
                if (logDO.getUpdateTime() == null) {
                    logDO.setUpdateTime(now);
                }
            }
            executionLogRepository.batchSave(logs);
        } catch (Exception e) {
            log.error("batchLog -> 批量保存日志失败, size={}", logs.size(), e);
        }
    }

    @Override
    public int cleanExpiredLogs(int days) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
        return executionLogRepository.cleanLogsBeforeTime(beforeTime);
    }

    @Override
    public Map<String, Object> getLogStatistics(Long accountId) {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime last7Days = today.minusDays(7);

        // 今日错误数
        long todayErrors = executionLogRepository.countErrorLogs(accountId, today, null);
        stats.put("todayErrors", todayErrors);

        // 近7天错误数
        long last7DaysErrors = executionLogRepository.countErrorLogs(accountId, last7Days, null);
        stats.put("last7DaysErrors", last7DaysErrors);

        // 最近日志
        List<ExecutionLogVO> recentLogs = listRecentByAccountId(accountId, 10);
        stats.put("recentLogs", recentLogs);

        return stats;
    }

    // ==================== 私有方法 ====================

    private AppStrategyExecutionLogDO buildBasicLog(AppAccountDO accountDO, ExecutionLogTypeEnum logType) {
        AppStrategyExecutionLogDO logDO = new AppStrategyExecutionLogDO();
        logDO.setId(SimpleSnowflake.nextId());
        logDO.setUserId(accountDO.getUserId());
        logDO.setAccountId(accountDO.getId());
        logDO.setStrategyInstanceId(accountDO.getStrategyInstanceId());
        logDO.setExchange(accountDO.getExchange());
        logDO.setSymbol(accountDO.getSymbol());
        logDO.setLogType(logType.getCode());
        logDO.setLogCategory(logType.getCategory());
        logDO.setMockData(TradeTypeEnum.MOCK.equalsByCode(accountDO.getTradeType())
                ? MockDataEnum.MOCK.code() : MockDataEnum.REAL.code());
        logDO.setCreateTime(LocalDateTime.now());
        logDO.setUpdateTime(LocalDateTime.now());
        return logDO;
    }

    private AppStrategyExecutionLogDO buildSessionLog(AppAccountSessionDO sessionDO, ExecutionLogTypeEnum logType) {
        AppStrategyExecutionLogDO logDO = new AppStrategyExecutionLogDO();
        logDO.setId(SimpleSnowflake.nextId());
        logDO.setUserId(sessionDO.getUserId());
        logDO.setAccountId(sessionDO.getAccountId());
        logDO.setStrategyInstanceId(sessionDO.getStrategyInstanceId());
        logDO.setSessionId(sessionDO.getId());
        logDO.setStrategyCode(sessionDO.getStrategyCode());
        logDO.setExchange(sessionDO.getExchange());
        logDO.setSymbol(sessionDO.getSymbol());
        logDO.setLogType(logType.getCode());
        logDO.setLogCategory(logType.getCategory());
        logDO.setHoldQty(sessionDO.getHoldQty());
        logDO.setHoldAvePrice(sessionDO.getHoldAvePrice());
        logDO.setMockData(sessionDO.getMockData());
        logDO.setCreateTime(LocalDateTime.now());
        logDO.setUpdateTime(LocalDateTime.now());
        return logDO;
    }

    private ExecutionLogVO convertToVO(AppStrategyExecutionLogDO logDO) {
        ExecutionLogVO vo = new ExecutionLogVO();
        BeanUtils.copyProperties(logDO, vo);

        // 设置日志类型描述
        ExecutionLogTypeEnum typeEnum = ExecutionLogTypeEnum.byCode(logDO.getLogType());
        if (typeEnum != null) {
            vo.setLogTypeDesc(typeEnum.getMessage());
        }

        return vo;
    }
}