package com.wuin.wi_mega.binance.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "币安持仓信息")
public class BinancePosition {

    @Schema(description = "交易对", example = "ETHUSDT")
    private String symbol;

    @Schema(description = "持仓方向：LONG=多头, SHORT=空头, BOTH=单向持仓模式", example = "LONG")
    private String positionSide;

    @Schema(description = "持仓数量（正数为多头，负数为空头）", example = "0.5")
    private BigDecimal positionAmt;

    @Schema(description = "开仓均价", example = "3114.95")
    private BigDecimal entryPrice;

    @Schema(description = "盈亏平衡价（含手续费）", example = "3116.20")
    private BigDecimal breakEvenPrice;

    @Schema(description = "当前标记价格", example = "3120.50")
    private BigDecimal markPrice;

    @JsonProperty("unRealizedProfit")
    @Schema(description = "未实现盈亏", example = "2.775")
    private BigDecimal unRealizedProfit;

    @Schema(description = "强平价格", example = "2850.00")
    private BigDecimal liquidationPrice;

    @Schema(description = "逐仓保证金（全仓模式下为0）", example = "0")
    private BigDecimal isolatedMargin;

    @Schema(description = "名义价值（持仓数量 * 标记价格）", example = "1560.25")
    private BigDecimal notional;

    @Schema(description = "保证金资产类型", example = "USDT")
    private String marginAsset;

    @Schema(description = "逐仓钱包余额（全仓模式下为0）", example = "0")
    private BigDecimal isolatedWallet;

    @Schema(description = "初始保证金", example = "78.01")
    private BigDecimal initialMargin;

    @Schema(description = "维持保证金", example = "7.80")
    private BigDecimal maintMargin;

    @Schema(description = "持仓初始保证金", example = "78.01")
    private BigDecimal positionInitialMargin;

    @Schema(description = "挂单初始保证金", example = "0")
    private BigDecimal openOrderInitialMargin;

    @Schema(description = "自动减仓指标（1-5，数值越小风险越低）", example = "2")
    private Integer adl;

    @Schema(description = "买单名义价值", example = "0")
    private BigDecimal bidNotional;

    @Schema(description = "卖单名义价值", example = "0")
    private BigDecimal askNotional;

    @Schema(description = "更新时间戳（毫秒）", example = "1736420887000")
    private Long updateTime;

    public static BinancePosition mock(String symbol, String positionSide, BigDecimal positionAmt, BigDecimal entryPrice, BigDecimal unRealizedProfit) {
        BinancePosition position = new BinancePosition();
        position.symbol = symbol;
        position.positionSide = positionSide;
        position.positionAmt = positionAmt;
        position.entryPrice = entryPrice;
        position.unRealizedProfit = unRealizedProfit;
        return position;
    }
}
