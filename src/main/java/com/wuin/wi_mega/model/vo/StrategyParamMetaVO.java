package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "策略参数元数据")
public class StrategyParamMetaVO {

    @Schema(description = "参数字段名")
    private String field;

    @Schema(description = "参数显示名称")
    private String label;

    @Schema(description = "参数类型: STRING, INTEGER, DECIMAL, BOOLEAN, ENUM")
    private String type;

    @Schema(description = "默认值")
    private Object defaultValue;

    @Schema(description = "是否必填")
    private Boolean required;

    @Schema(description = "参数说明")
    private String description;

    @Schema(description = "枚举选项（type=ENUM时使用）")
    private Object[] options;

    @Schema(description = "最小值")
    private Object min;

    @Schema(description = "最大值")
    private Object max;

    @Schema(description = "步长")
    private Object step;

    @Schema(description = "单位")
    private String unit;
}
