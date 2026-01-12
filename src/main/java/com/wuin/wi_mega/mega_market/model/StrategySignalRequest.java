package com.wuin.wi_mega.mega_market.model;

import com.wuin.wi_mega.common.enums.KlineIntervalEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class StrategySignalRequest {
    @Parameter(description = "交易对", example = "ETHUSDT")
    private String symbol;
    @Parameter(description = "时间周期", example = "1m")
    private KlineIntervalEnum interval;
    @Parameter(description = "当前持仓方向")
    private String positionSide;
    // 策略配置参数
    @Parameter(description = "是否双向持仓")
    private Boolean dualMode = true;
    @Parameter(description = "RSI周期")
    private Integer rsiPeriod = 4;
    @Parameter(description = "RSI超卖线")
    private Double rsiOversold = 25.0;
    @Parameter(description = "RSI超买线")
    private Double rsiOverbought = 75.0;
    @Parameter(description = "EMA快线")
    private Integer emaFast = 3;
    @Parameter(description = "EMA慢线")
    private Integer emaSlow = 7;
    @Parameter(description = "emaSig")
    private Integer emaSig = 3;
    @Parameter(description = "成交量周期")
    private Integer volumePeriod = 3;
    @Parameter(description = "成交量阈值")
    private Double volumeThreshold = 1.2;
    @Parameter(description = "最小评分阈值")
    private Double minScore = 45.0;

    private int atrPeriod = 4;

    @Parameter(description = "初始化数据K线数量")
    private Integer initKlineCount = 24;

    private Boolean allowLong = true;

    private Boolean allowShort = true;

    private Boolean allowCloseLong = false;

    private Boolean allowCloseShort = false;
}