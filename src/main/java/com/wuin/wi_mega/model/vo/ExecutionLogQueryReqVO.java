package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行日志查询请求
 */
@Data
@Schema(description = "执行日志查询请求")
public class ExecutionLogQueryReqVO {

    @Schema(description = "页码，默认1")
    private Integer page = 1;

    @Schema(description = "每页数量，默认20")
    private Integer pageSize = 20;

    @Schema(description = "账号ID")
    private Long accountId;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "策略实例ID")
    private Long strategyInstanceId;

    @Schema(description = "日志类型编码")
    private Integer logType;

    @Schema(description = "日志类别：SIGNAL/OPEN/APPEND/REVERSE/TAKE_PROFIT/STOP_LOSS/SESSION/STRATEGY/ORDER/SYSTEM")
    private String logCategory;

    @Schema(description = "日志级别：INFO/WARN/ERROR")
    private String logLevel;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "关键词搜索（匹配标题或内容）")
    private String keyword;
}

