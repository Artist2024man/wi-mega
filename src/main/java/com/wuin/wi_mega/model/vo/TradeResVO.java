package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "交易结果")
public class TradeResVO {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "客户端订单ID")
    private String clientOrderId;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "方向: LONG/SHORT")
    private String positionSide;

    @Schema(description = "订单类型: OPEN/CLOSE/ADD")
    private String orderType;

    @Schema(description = "成交数量")
    private BigDecimal quantity;

    @Schema(description = "成交均价")
    private BigDecimal avgPrice;

    @Schema(description = "成交金额")
    private BigDecimal amount;

    @Schema(description = "手续费")
    private BigDecimal fee;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "消息")
    private String message;
}
