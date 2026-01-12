package com.wuin.wi_mega.model.bo.base;

import com.wuin.wi_mega.common.enums.KlineIntervalEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StrategyMartingaleBaseParam implements StrategyBaseParam {

    @Schema(description = "使用的K线周期", requiredMode = Schema.RequiredMode.REQUIRED)
    private KlineIntervalEnum interval;
    @Schema(description = "RSI周期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rsiPeriod = 4;
    @Schema(description = "RSI超卖线", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double rsiOversold = 25.0;
    @Schema(description = "RSI超买线", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double rsiOverbought = 75.0;
    @Schema(description = "EMA快线", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer emaFast = 9;
    @Schema(description = "EMA慢线", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer emaSlow = 7;
    @Schema(description = "成交量周期", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer volumePeriod = 3;
    @Schema(description = "成交量阈值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double volumeThreshold = 1.2;
    @Schema(description = "最小评分阈值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double minScore = 45.0;

}
