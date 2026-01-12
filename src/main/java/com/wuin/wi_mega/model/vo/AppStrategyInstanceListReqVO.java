package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class AppStrategyInstanceListReqVO {

    @Schema(description = "所属策略ID")
    private Long strategyId;

    @Schema(description = "策略名称")
    private String name;

    @Schema(description = "交易平台")
    private String exchange;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "上下架状态：1=已上架，2=已下架")
    private Integer status;
}
