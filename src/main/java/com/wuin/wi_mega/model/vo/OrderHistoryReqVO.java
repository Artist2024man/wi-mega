package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "订单历史查询参数")
public class OrderHistoryReqVO {

    @Schema(description = "账号ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long accountId;

    @Schema(description = "会话ID（可选，用于查询某个会话下的所有订单）")
    private Long sessionId;

    @Schema(description = "订单类型：1=开仓，2=平仓")
    private Integer orderType;

    @Schema(description = "订单状态：1=新建，2=部分成交，3=全部成交，4=已撤销，5=被拒绝，6=过期")
    private Integer status;

    @Schema(description = "持仓方向：LONG=多，SHORT=空")
    private String positionSide;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

}
