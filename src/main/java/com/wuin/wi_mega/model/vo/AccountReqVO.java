package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Schema(description = "开始策略请求参数")
@NoArgsConstructor
@AllArgsConstructor
public class AccountReqVO {

    @Schema(description = "账号ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long accountId;

    @Schema(description = "策略执行允许的最小价格")
    private BigDecimal strategyMinPrice;

    @Schema(description = "策略执行允许的最大价格")
    private BigDecimal strategyMaxPrice;

    public AccountReqVO(Long accountId) {
        this.accountId = accountId;
    }
}
