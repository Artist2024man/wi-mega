package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.repository.domain.AppStrategyExecutionLogDO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 策略执行日志Repository接口
 */
public interface AppStrategyExecutionLogRepository extends IService<AppStrategyExecutionLogDO> {

    /**
     * 分页查询日志
     * @param userId 用户ID
     * @param accountId 账号ID
     * @param sessionId 会话ID
     * @param strategyInstanceId 策略实例ID
     * @param logType 日志类型
     * @param logCategory 日志类别
     * @param logLevel 日志级别
     * @param symbol 交易对
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<AppStrategyExecutionLogDO> pageList(
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
            int pageSize
    );

    /**
     * 根据会话ID查询日志
     * @param sessionId 会话ID
     * @return 日志列表
     */
    List<AppStrategyExecutionLogDO> listBySessionId(Long sessionId);

    /**
     * 根据账号ID查询最近日志
     * @param accountId 账号ID
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AppStrategyExecutionLogDO> listRecentByAccountId(Long accountId, int limit);

    /**
     * 根据策略实例ID查询日志
     * @param strategyInstanceId 策略实例ID
     * @param limit 数量限制
     * @return 日志列表
     */
    List<AppStrategyExecutionLogDO> listByStrategyInstanceId(Long strategyInstanceId, int limit);

    /**
     * 批量保存日志
     * @param logs 日志列表
     * @return 是否成功
     */
    boolean batchSave(List<AppStrategyExecutionLogDO> logs);

    /**
     * 清理指定时间之前的日志
     * @param beforeTime 时间阈值
     * @return 删除条数
     */
    int cleanLogsBeforeTime(LocalDateTime beforeTime);

    /**
     * 统计错误日志数量
     * @param accountId 账号ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 错误数量
     */
    long countErrorLogs(Long accountId, LocalDateTime startTime, LocalDateTime endTime);
}

