package com.wuin.wi_mega.binance.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FuturesTradeDTO {

    /** 交易对 */
    private String symbol;

    /** 成交ID */
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 买卖方向：BUY / SELL */
    private String side;

    /** 成交价格 */
    private BigDecimal price;

    /** 成交数量 */
    private BigDecimal qty;

    /** 已实现盈亏 */
    private BigDecimal realizedPnl;

    /** 成交额 */
    private BigDecimal quoteQty;

    /** 手续费 */
    private BigDecimal commission;

    /** 手续费资产 */
    private String commissionAsset;

    /** 成交时间（毫秒） */
    private Long time;

    /** 持仓方向：LONG / SHORT */
    private String positionSide;

    /** 是否买方 */
    private Boolean buyer;

    /** 是否是 maker */
    private Boolean maker;
}
