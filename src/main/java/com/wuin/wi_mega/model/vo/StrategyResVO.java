package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "策略响应")
public class StrategyResVO {

    @Schema(description = "策略名称")
    private String name;

    @Schema(description = "策略编码")
    private String code;

    @Schema(description = "密码")
    private String apiKeyPass;

    @Schema(description = "平台")
    private String exchange;

    @Schema(description = "初始净值")
    private BigDecimal initEquity;

    @Schema(description = "计价单位")
    private String equityCoin;

    @Schema(description = "备注说明")
    private String remark;

}
