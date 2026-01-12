package com.wuin.wi_mega.model.bo;

import com.alibaba.fastjson2.JSONObject;
import com.wuin.wi_mega.common.enums.BaOrderStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderBizBO {

    @Schema(description = "业务订单ID")
    private String clientOrderId;

    @Schema(description = "ba订单编号")
    private Long orderId;

    @Schema(description = "状态")
    private BaOrderStatusEnum status;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "买卖类型，BUY=买入，SELL=卖出")
    private String side;

    @Schema(description = "方向，LONG=多头，SHORT=空头")
    private String position;

    @Schema(description = "下次同步时间")
    private LocalDateTime nextSyncTime;

    @Schema(description = "订单信息")
    private JSONObject orderInfo;





}
