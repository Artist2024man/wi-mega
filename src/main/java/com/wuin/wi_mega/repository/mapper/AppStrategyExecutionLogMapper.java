package com.wuin.wi_mega.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuin.wi_mega.repository.domain.AppStrategyExecutionLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 策略执行日志Mapper接口
 */
@Mapper
public interface AppStrategyExecutionLogMapper extends BaseMapper<AppStrategyExecutionLogDO> {

    /**
     * 批量插入日志
     * @param logs 日志列表
     * @return 插入条数
     */
    int batchInsert(@Param("logs") List<AppStrategyExecutionLogDO> logs);

    /**
     * 清理指定时间之前的日志
     * @param beforeTime 时间阈值
     * @return 删除条数
     */
    int cleanLogsBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 统计各类型日志数量
     * @param accountId 账号ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    List<AppStrategyExecutionLogDO> countByLogType(
            @Param("accountId") Long accountId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}

