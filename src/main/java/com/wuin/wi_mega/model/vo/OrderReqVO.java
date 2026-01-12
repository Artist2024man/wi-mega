package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "订单请求参数")
public class OrderReqVO {

    @Schema(description = "账号ID")
    private Long accountId;

    @Schema(description = "订单ID")
    private String orderNo;

}
