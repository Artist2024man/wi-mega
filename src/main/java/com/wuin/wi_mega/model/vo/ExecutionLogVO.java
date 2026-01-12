package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 执行日志响应VO
 */
@Data
@Schema(description = "执行日志响应")
public class ExecutionLogVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "账号ID")
    private Long accountId;

    @Schema(description = "策略实例ID")
    private Long strategyInstanceId;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "策略编码")
    private String strategyCode;

    @Schema(description = "交易平台")
    private String exchange;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "日志类型编码")
    private Integer logType;

    @Schema(description = "日志类型描述")
    private String logTypeDesc;

    @Schema(description = "日志类别")
    private String logCategory;

    @Schema(description = "日志级别")
    private String logLevel;

    @Schema(description = "日志标题")
    private String title;

    @Schema(description = "日志内容")
    private String content;

    @Schema(description = "当前价格")
    private BigDecimal currentPrice;

    @Schema(description = "目标价格")
    private BigDecimal targetPrice;

    @Schema(description = "持仓数量")
    private BigDecimal holdQty;

    @Schema(description = "持仓均价")
    private BigDecimal holdAvePrice;

    @Schema(description = "盈亏金额")
    private BigDecimal pnl;

    @Schema(description = "持仓方向")
    private String positionSide;

    @Schema(description = "执行结果：SUCCESS/FAILED/SKIPPED")
    private String result;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "扩展信息")
    private String extInfo;

    @Schema(description = "是否模拟数据：1=是，0=否")
    private Integer mockData;

    @Schema(description = "执行耗时（毫秒）")
    private Long executionTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

