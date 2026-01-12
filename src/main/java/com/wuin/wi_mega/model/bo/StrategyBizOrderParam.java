package com.wuin.wi_mega.model.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class StrategyBizOrderParam {

    private Integer idx = 0;

    @Schema(description = "开仓方向")
    private String position;

    @Schema(description = "开仓价格")
    private BigDecimal price;

    @Schema(description = "开仓数量")
    private BigDecimal qty;

    @Schema(description = "开仓订单ID")
    private Long orderId;

    @Schema(description = "开仓均价")
    private BigDecimal avgPrice;

    public boolean isDone() {
        return orderId != null;
    }

    public StrategyBizOrderParam(Integer idx, String position, BigDecimal qty) {
        this.idx = idx;
        this.position = position;
        this.qty = qty;
    }
}
