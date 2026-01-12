package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改账号信息")
public class AccountUpdateReqVO {

    @Schema(description = "账号ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "账号名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "API KEY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiKey;

    @Schema(description = "PRIV KEY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiKeyPass;

    @Schema(description = "持仓模式：1=单向持仓，2=双向持仓", requiredMode = Schema.RequiredMode.REQUIRED)
    private int dualSidePosition;

    @Schema(description = "杠杆倍数，支持:1-125", requiredMode = Schema.RequiredMode.REQUIRED)
    private int leverage;

    @Schema(description = "策略实例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long strategyInstanceId;

    @Schema(description = "交易模式：1=模拟，2=实盘")
    private Integer tradeType;
}
