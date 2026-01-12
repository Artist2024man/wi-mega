package com.wuin.wi_mega.model.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrategyStatBO {

    /**
     * 总交易次数（已完成的会话数）
     */
    private Integer totalCount = 0;

    /**
     * 盈利次数
     */
    private Integer winCount = 0;

    /**
     * 亏损次数
     */
    private Integer loseCount = 0;

    /**
     * 总盈亏
     */
    private BigDecimal totalPnl = BigDecimal.ZERO;

    /**
     * 总盈利金额
     */
    private BigDecimal totalProfit = BigDecimal.ZERO;

    /**
     * 总亏损金额
     */
    private BigDecimal totalLoss = BigDecimal.ZERO;

    /**
     * 最大单笔盈利
     */
    private BigDecimal maxProfit = BigDecimal.ZERO;

    /**
     * 最大单笔亏损
     */
    private BigDecimal maxLoss = BigDecimal.ZERO;

    /**
     * 总开仓手续费
     */
    private BigDecimal totalOpenFee = BigDecimal.ZERO;

    /**
     * 总平仓手续费
     */
    private BigDecimal totalCloseFee = BigDecimal.ZERO;

}
