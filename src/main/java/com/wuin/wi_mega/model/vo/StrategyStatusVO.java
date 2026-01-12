package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Schema(description = "策略运行状态汇总")
public class StrategyStatusVO {

    @Schema(description = "账号ID")
    private Long accountId;

    @Schema(description = "账号名称")
    private String accountName;

    @Schema(description = "策略实例ID")
    private Long strategyInstanceId;

    @Schema(description = "策略名称")
    private String strategyName;

    @Schema(description = "策略编码")
    private String strategyCode;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "策略运行状态：1=运行中，0=已停止")
    private Integer strategyStatus;

    @Schema(description = "策略运行状态名称")
    private String strategyStatusName;

    // ========== 累计统计 ==========

    @Schema(description = "累计交易次数")
    private Integer totalTradeCount = 0;

    @Schema(description = "累计盈利次数")
    private Integer totalWinCount = 0;

    @Schema(description = "累计亏损次数")
    private Integer totalLoseCount = 0;

    @Schema(description = "累计胜率（%）")
    private BigDecimal totalWinRate = BigDecimal.ZERO;

    @Schema(description = "累计盈亏")
    private BigDecimal totalPnl = BigDecimal.ZERO;

    @Schema(description = "累计盈利金额")
    private BigDecimal totalProfit = BigDecimal.ZERO;

    @Schema(description = "累计亏损金额")
    private BigDecimal totalLoss = BigDecimal.ZERO;

    @Schema(description = "累计净盈亏（扣除手续费）")
    private BigDecimal totalNetPnl = BigDecimal.ZERO;

    @Schema(description = "累计手续费")
    private BigDecimal totalFee = BigDecimal.ZERO;

    @Schema(description = "平均盈利")
    private BigDecimal avgProfit = BigDecimal.ZERO;

    @Schema(description = "平均亏损")
    private BigDecimal avgLoss = BigDecimal.ZERO;

    @Schema(description = "盈亏比")
    private BigDecimal profitLossRatio = BigDecimal.ZERO;

    @Schema(description = "最大单笔盈利")
    private BigDecimal maxProfit = BigDecimal.ZERO;

    @Schema(description = "最大单笔亏损")
    private BigDecimal maxLoss = BigDecimal.ZERO;

    // ========== 今日统计 ==========

    @Schema(description = "今日交易次数")
    private Integer todayTradeCount = 0;

    @Schema(description = "今日盈利次数")
    private Integer todayWinCount = 0;

    @Schema(description = "今日亏损次数")
    private Integer todayLoseCount = 0;

    @Schema(description = "今日胜率（%）")
    private BigDecimal todayWinRate = BigDecimal.ZERO;

    @Schema(description = "今日盈亏")
    private BigDecimal todayPnl = BigDecimal.ZERO;

    @Schema(description = "今日净盈亏（扣除手续费）")
    private BigDecimal todayNetPnl = BigDecimal.ZERO;

    @Schema(description = "今日手续费")
    private BigDecimal todayFee = BigDecimal.ZERO;

    /**
     * 根据统计数据计算衍生指标
     */
    public void calculate() {
        // 计算累计胜率
        if (totalTradeCount > 0) {
            this.totalWinRate = new BigDecimal(totalWinCount)
                    .divide(new BigDecimal(totalTradeCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // 计算今日胜率
        if (todayTradeCount > 0) {
            this.todayWinRate = new BigDecimal(todayWinCount)
                    .divide(new BigDecimal(todayTradeCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // 计算平均盈利
        if (totalWinCount > 0 && totalProfit.compareTo(BigDecimal.ZERO) > 0) {
            this.avgProfit = totalProfit.divide(new BigDecimal(totalWinCount), 4, RoundingMode.HALF_UP);
        }

        // 计算平均亏损
        if (totalLoseCount > 0 && totalLoss.compareTo(BigDecimal.ZERO) > 0) {
            this.avgLoss = totalLoss.divide(new BigDecimal(totalLoseCount), 4, RoundingMode.HALF_UP);
        }

        // 计算盈亏比
        if (avgLoss.compareTo(BigDecimal.ZERO) > 0) {
            this.profitLossRatio = avgProfit.divide(avgLoss, 4, RoundingMode.HALF_UP);
        }

        // 计算累计净盈亏
        this.totalNetPnl = totalPnl.subtract(totalFee);

        // 计算今日净盈亏
        this.todayNetPnl = todayPnl.subtract(todayFee);
    }
}
