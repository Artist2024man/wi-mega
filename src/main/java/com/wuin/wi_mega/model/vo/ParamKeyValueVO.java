package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "参数键值对")
public class ParamKeyValueVO {

    @Schema(description = "参数键名(英文)")
    private String key;

    @Schema(description = "参数名称(中文)")
    private String label;

    @Schema(description = "参数值")
    private Object value;

    @Schema(description = "参数说明")
    private String description;

}
