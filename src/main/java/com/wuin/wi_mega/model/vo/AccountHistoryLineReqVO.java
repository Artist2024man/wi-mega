package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账户历史净值请求参数")
public class AccountHistoryLineReqVO {

    @Schema(description = "开始时间:yyyy-MM-dd HH:mm:ss", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "结束时间:yyyy-MM-dd HH:mm:ss", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @Schema(description = "账号ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long accountId;

}
