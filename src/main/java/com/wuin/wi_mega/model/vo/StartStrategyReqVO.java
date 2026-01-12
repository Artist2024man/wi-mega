package com.wuin.wi_mega.model.vo;

import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.model.bo.running.StrategyOneMinRunParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "开始策略请求参数")
public class StartStrategyReqVO {

    @Schema(description = "账号ID")
    private String accountId;

    @Schema(description = "KEY")
    private String apiKey;

    @Schema(description = "密码")
    private String apiKeyPass;

    @Schema(description = "策略")
    private StrategyEnum strategy;

    @Schema(description = "策略参数")
    private StrategyOneMinRunParam param;

}
