package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改账号策略信息")
public class AccountStrategyUpdateReqVO {

    @Schema(description = "账号ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "杠杆倍数，支持:1-125", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer leverage;

    @Schema(description = "策略实例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long strategyInstanceId;

}
