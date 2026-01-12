package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "条件单下单请求")
public class AlgoOrderReqVO {

    @Schema(description = "账号ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long accountId;

    @Schema(description = "会话ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long sessionId;

    @Schema(description = "持仓方向", requiredMode = Schema.RequiredMode.REQUIRED)
    private String positionSide;

    @Schema(description = "止盈价格", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal takeProfitPrice;

    @Schema(description = "止损价格", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal stopLossPrice;

}
