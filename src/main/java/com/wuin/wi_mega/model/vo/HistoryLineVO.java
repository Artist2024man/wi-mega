package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "净值历史折线图")
public class HistoryLineVO {

    @Schema(description = "X轴")
    private List<String> lineX = new ArrayList<>();

    @Schema(description = "Y轴")
    private List<BigDecimal> lineY = new ArrayList<>();

}
