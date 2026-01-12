package com.wuin.wi_mega.model.bo.running;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StrategyRunParam {

    @Schema(description = "最小止盈", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal tp;

    @Schema(description = "单笔止损金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal sl;

    @Schema(description = "开仓数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal opqty;

    @Schema(description = "补仓数据", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<StrategyMartingaleRunParam.QtyPriceBO> appends;

    @Schema(description = "对冲亏损金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal rslos;

    @Data
    public static class QtyPriceBO {

        @Schema(description = "反向点数(开仓均价反向)", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal los;

        @Schema(description = "补仓数量", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal qty;

    }

}
