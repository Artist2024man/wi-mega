package com.wuin.wi_mega.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PageResponseVO", title = "分页请求统一响应参数")
public class PageResponseVO<T> {

    @Schema(description = "总数据量")
    private Long total = 0L;

    @Schema(description = "数据列表")
    private List<T> records = new ArrayList<>();

}
