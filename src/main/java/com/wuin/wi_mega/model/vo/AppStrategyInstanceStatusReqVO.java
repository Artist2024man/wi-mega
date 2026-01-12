package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "策略上下架请求")
public class AppStrategyInstanceStatusReqVO {

    @Schema(description = "策略实例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "上下架状态：1=已上架，2=已下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

}
