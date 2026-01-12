package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 持仓信息响应（用于Swagger文档展示）
 */
@Data
@Schema(description = "持仓信息响应")
public class PositionRiskResponse {

    @Schema(description = "响应码", example = "200")
    private Integer code;

    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "响应描述", example = "success")
    private String description;

    @Schema(description = "持仓数据列表")
    private List<PositionDetailVO> data;

    @Schema(description = "请求ID")
    private String requestId;
}
