package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 持仓详情VO（包含策略业务信息）
 */
@Data
@Schema(description = "持仓详情")
public class PositionDetailVO {

    // ========== 基础持仓信息 ==========
    @Schema(description = "交易对", example = "ETHUSDT")
    private String symbol;

    @Schema(description = "持仓方向：LONG=多头, SHORT=空头", example = "LONG")
    private String positionSide;

    @Schema(description = "持仓数量", example = "0.5")
    private BigDecimal positionAmt;

    @Schema(description = "开仓均价", example = "3114.95")
    private BigDecimal entryPrice;

    @Schema(description = "当前标记价格", example = "3120.50")
    private BigDecimal markPrice;

    @Schema(description = "未实现盈亏", example = "2.775")
    private BigDecimal unRealizedProfit;

    @Schema(description = "未实现盈亏百分比", example = "0.89")
    private BigDecimal unRealizedProfitPercent;

    @Schema(description = "盈亏平衡价", example = "3116.20")
    private BigDecimal breakEvenPrice;

    @Schema(description = "强平价格", example = "2850.00")
    private BigDecimal liquidationPrice;

    @Schema(description = "名义价值", example = "1560.25")
    private BigDecimal notional;

    @Schema(description = "初始保证金", example = "78.01")
    private BigDecimal initialMargin;

    @Schema(description = "维持保证金", example = "7.80")
    private BigDecimal maintMargin;

    @Schema(description = "保证金资产类型", example = "USDT")
    private String marginAsset;

    @Schema(description = "杠杆倍数", example = "20")
    private Integer leverage;

    @Schema(description = "更新时间戳", example = "1736420887000")
    private Long updateTime;

    // ========== 策略业务信息 ==========
    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "止盈价格", example = "3150.00")
    private BigDecimal takeProfitPrice;

    @Schema(description = "止损价格", example = "3050.00")
    private BigDecimal stopLossPrice;

    @Schema(description = "已加仓次数", example = "1")
    private Integer appendCount;

    @Schema(description = "最大加仓次数", example = "2")
    private Integer maxAppendCount;

    @Schema(description = "下次加仓价格", example = "3080.00")
    private BigDecimal nextAppendPrice;

    @Schema(description = "反仓触发价格", example = "3000.00")
    private BigDecimal reversePrice;

    @Schema(description = "是否已触发反仓", example = "false")
    private Boolean reversed;

    @Schema(description = "策略编码", example = "MARTINGALE")
    private String strategyCode;

    @Schema(description = "是否模拟交易", example = "true")
    private Boolean mockData;
}
