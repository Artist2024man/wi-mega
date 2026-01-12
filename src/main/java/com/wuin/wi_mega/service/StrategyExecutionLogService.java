package com.wuin.wi_mega.service;

import com.wuin.wi_mega.common.enums.ExecutionLogTypeEnum;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.vo.ExecutionLogQueryReqVO;
import com.wuin.wi_mega.model.vo.ExecutionLogVO;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.repository.domain.AppStrategyExecutionLogDO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 策略执行日志服务接口
 */
public interface StrategyExecutionLogService {

    /**
     * 分页查询日志
     * @param userId 当前用户ID
     * @param reqVO 查询条件
     * @return 分页结果
     */
    PageResponseVO<ExecutionLogVO> pageList(Long userId, ExecutionLogQueryReqVO reqVO);

    /**
     * 根据会话ID查询日志
     * @param sessionId 会话ID
     * @return 日志列表
     */
    List<ExecutionLogVO> listBySessionId(Long sessionId);

    /**
     * 根据账号ID查询最近日志
     * @param accountId 账号ID
     * @param limit 数量限制
     * @return 日志列表
     */
    List<ExecutionLogVO> listRecentByAccountId(Long accountId, int limit);

    /**
     * 获取日志详情
     * @param logId 日志ID
     * @return 日志详情
     */
    ExecutionLogVO getLogDetail(Long logId);

    // ==================== 日志记录方法 ====================

    /**
     * 记录日志（通用方法）
     * @param logDO 日志对象
     */
    void log(AppStrategyExecutionLogDO logDO);

    /**
     * 记录信号日志
     * @param accountDO 账号信息
     * @param logType 日志类型
     * @param title 标题
     * @param content 内容
     * @param currentPrice 当前价格
     * @param positionSide 持仓方向
     */
    void logSignal(AppAccountDO accountDO, ExecutionLogTypeEnum logType,
                   String title, String content, BigDecimal currentPrice, String positionSide);

    /**
     * 记录会话日志
     * @param sessionDO 会话信息
     * @param logType 日志类型
     * @param title 标题
     * @param content 内容
     * @param result 执行结果
     */
    void logSession(AppAccountSessionDO sessionDO, ExecutionLogTypeEnum logType,
                    String title, String content, String result);

    /**
     * 记录会话日志（带价格信息）
     * @param sessionDO 会话信息
     * @param logType 日志类型
     * @param title 标题
     * @param content 内容
     * @param currentPrice 当前价格
     * @param targetPrice 目标价格
     * @param result 执行结果
     */
    void logSessionWithPrice(AppAccountSessionDO sessionDO, ExecutionLogTypeEnum logType,
                             String title, String content,
                             BigDecimal currentPrice, BigDecimal targetPrice, String result);

    /**
     * 记录订单日志
     * @param accountDO 账号信息
     * @param sessionId 会话ID
     * @param orderId 订单ID
     * @param logType 日志类型
     * @param title 标题
     * @param content 内容
     * @param price 价格
     * @param qty 数量
     * @param result 结果
     */
    void logOrder(AppAccountDO accountDO, Long sessionId, Long orderId,
                  ExecutionLogTypeEnum logType, String title, String content,
                  BigDecimal price, BigDecimal qty, String result);

    /**
     * 记录错误日志
     * @param accountDO 账号信息
     * @param sessionId 会话ID
     * @param title 标题
     * @param errorMsg 错误信息
     */
    void logError(AppAccountDO accountDO, Long sessionId, String title, String errorMsg);

    /**
     * 批量记录日志
     * @param logs 日志列表
     */
    void batchLog(List<AppStrategyExecutionLogDO> logs);

    /**
     * 清理过期日志（默认保留30天）
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanExpiredLogs(int days);

    /**
     * 统计日志概览
     * @param accountId 账号ID
     * @return 统计结果
     */
    Map<String, Object> getLogStatistics(Long accountId);
}

