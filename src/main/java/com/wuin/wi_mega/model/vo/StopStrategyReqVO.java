package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "开始策略请求参数")
public class StopStrategyReqVO {

    @Schema(description = "账号ID")
    private String accountId;

}
