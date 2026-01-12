package com.wuin.wi_mega.model.vo;

import com.wuin.wi_mega.common.enums.ExchangeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户历史净值请求参数")
public class UserHistoryLineReqVO {

    @Schema(description = "开始时间:yyyy-MM-dd HH:mm:ss", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "结束时间:yyyy-MM-dd HH:mm:ss", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @Schema(description = "平台:BINANCE", requiredMode = Schema.RequiredMode.REQUIRED)
    private ExchangeEnum exchange;

}
