package com.wuin.wi_mega.model.bo.running;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrategyOneMinRunParam extends StrategyRunParam {

    @Schema(description = "多单挂单当前最低价往上百分比", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal lbuf;

    @Schema(description = "空单挂单当前最高价往下百分比", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal sbuf;


}
