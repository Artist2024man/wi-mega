package com.wuin.wi_mega.repository.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wuin.wi_mega.repository.base.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("stat_account_equity_log_day")
public class StatAccountEquityLogDayDO extends BaseDO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "账户ID")
    private Long accountId;

    @Schema(description = "交易所")
    private String exchange;

    @Schema(description = "净值")
    private BigDecimal equity;

    @Schema(description = "分钟：yyyyMMdd")
    private Long timeLong;

}
