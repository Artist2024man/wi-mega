package com.wuin.wi_mega.repository.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wuin.wi_mega.common.enums.SessionBizStatusEnum;
import com.wuin.wi_mega.repository.base.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("app_account_session")
public class AppAccountSessionDO extends BaseDO {

    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "账号ID")
    private Long accountId;
    @Schema(description = "所属策略ID")
    private Long strategyInstanceId;
    @Schema(description = "策略编码")
    private String strategyCode;
    @Schema(description = "交易平台")
    private String exchange;
    @Schema(description = "交易对")
    private String symbol;
    @Schema(description = "基础参数")
    private String baseParam;
    @Schema(description = "运行参数")
    private String runParam;
    @Schema(description = "业务参数")
    private String bizParam;

    @Schema(description = "持仓均价")
    private BigDecimal holdAvePrice;
    @Schema(description = "持仓数量")
    private BigDecimal holdQty;
    @Schema(description = "盈亏情况")
    private BigDecimal closePnl;

    @Schema(description = "开仓手续费")
    private BigDecimal openFee;

    @Schema(description = "结束手续费")
    private BigDecimal closeFee;

    @Schema(description = "下次执行时间")
    private LocalDateTime nextCheckTime;

    @Schema(description = "止盈价格")
    private BigDecimal takeProfitPrice;
    @Schema(description = "止盈订单ID（业务）")
    private String takeProfitClientOrderId;
    @Schema(description = "止盈订单ID")
    private Long takeProfitOrderId;
    @Schema(description = "止损价格")
    private BigDecimal stopLossPrice;
    @Schema(description = "止损订单ID（业务）")
    private String stopLossClientOrderId;
    @Schema(description = "止损订单ID")
    private Long stopLossOrderId;

    @Schema(description = "条件单止损订单ID")
    private Long stopLossAlgoId;

    @Schema(description = "条件单止盈订单ID")
    private Long takeProfitAlgoId;

    @Schema(description = "最终订单同步状态：1=完成，0=未完成")
    private Integer syncStatus;
    /**
     * @see com.wuin.wi_mega.common.enums.SessionStatusEnum
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * @see SessionBizStatusEnum
     */
    @Schema(description = "业务状态")
    private Integer bizStatus;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "是否模拟数据：1=是，0=否")
    private Integer mockData;

}
