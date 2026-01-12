package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@Slf4j
public class AppStrategyInstanceCreateReqVO {

    @Schema(description = "ID(更新策略时必须携带)")
    private Long id;

    @Schema(description = "所属策略ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long strategyId;

    @Schema(description = "策略名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "交易平台", requiredMode = Schema.RequiredMode.REQUIRED)
    private String exchange;

    @Schema(description = "交易对", requiredMode = Schema.RequiredMode.REQUIRED)
    private String symbol;

    @Schema(description = "基础参数（动态JSON）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> baseParam;

    @Schema(description = "运行参数（动态JSON）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> runParam;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "上下架状态：1=已上架，2=已下架，默认已下架")
    private Integer status;
}
