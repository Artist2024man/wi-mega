package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "策略参数模板")
public class StrategyParamTemplateVO {

    @Schema(description = "策略ID")
    private Long strategyId;

    @Schema(description = "策略编码")
    private String code;

    @Schema(description = "策略名称")
    private String name;

    @Schema(description = "基础参数元数据列表")
    private List<StrategyParamMetaVO> baseParamMeta;

    @Schema(description = "运行参数元数据列表")
    private List<StrategyParamMetaVO> runParamMeta;

    @Schema(description = "基础参数默认值")
    private Map<String, Object> baseParamDefaults;

    @Schema(description = "运行参数默认值")
    private Map<String, Object> runParamDefaults;
}
