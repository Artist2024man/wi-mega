package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "交易请求")
public class TradeReqVO {

    @Schema(description = "账号ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long accountId;

    @Schema(description = "交易数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantity;

    @Schema(description = "会话ID（加仓/平仓时可选，不传则使用当前运行中的会话）")
    private Long sessionId;

    @Schema(description = "价格（限价单时使用，市价单不需要）")
    private BigDecimal price;
}
