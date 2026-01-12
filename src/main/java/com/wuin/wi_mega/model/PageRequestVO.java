package com.wuin.wi_mega.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Schema(description = "PageRequestVO", title = "分页请求统一参数")
public class PageRequestVO<T> {

    @Schema(description = "页码，默认1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer page = 1;

    @Schema(description = "每页数据量，默认10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pageSize = 10;

    @Schema(description = "其他筛选条件", requiredMode = Schema.RequiredMode.REQUIRED)
    private T param;

}
