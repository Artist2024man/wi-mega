package com.wuin.wi_mega.model.vo;

import com.wuin.wi_mega.common.enums.SessionStatusEnum;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "会话历史记录")
public class SessionHistoryVO {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "账号ID")
    private Long accountId;

    @Schema(description = "策略实例ID")
    private Long strategyInstanceId;

    @Schema(description = "策略编码")
    private String strategyCode;

    @Schema(description = "交易平台")
    private String exchange;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "持仓均价")
    private BigDecimal holdAvePrice;

    @Schema(description = "持仓数量")
    private BigDecimal holdQty;

    @Schema(description = "已实现盈亏")
    private BigDecimal closePnl;

    @Schema(description = "开仓手续费")
    private BigDecimal openFee;

    @Schema(description = "平仓手续费")
    private BigDecimal closeFee;

    @Schema(description = "净盈亏（盈亏-手续费）")
    private BigDecimal netPnl;

    @Schema(description = "止盈价格")
    private BigDecimal takeProfitPrice;

    @Schema(description = "止损价格")
    private BigDecimal stopLossPrice;

    @Schema(description = "状态码")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;

    public SessionHistoryVO() {
    }

    public SessionHistoryVO(AppAccountSessionDO sessionDO) {
        this.id = sessionDO.getId();
        this.accountId = sessionDO.getAccountId();
        this.strategyInstanceId = sessionDO.getStrategyInstanceId();
        this.strategyCode = sessionDO.getStrategyCode();
        this.exchange = sessionDO.getExchange();
        this.symbol = sessionDO.getSymbol();
        this.holdAvePrice = sessionDO.getHoldAvePrice();
        this.holdQty = sessionDO.getHoldQty();
        this.closePnl = sessionDO.getClosePnl() != null ? sessionDO.getClosePnl() : BigDecimal.ZERO;
        this.openFee = sessionDO.getOpenFee() != null ? sessionDO.getOpenFee() : BigDecimal.ZERO;
        this.closeFee = sessionDO.getCloseFee() != null ? sessionDO.getCloseFee() : BigDecimal.ZERO;
        this.takeProfitPrice = sessionDO.getTakeProfitPrice();
        this.stopLossPrice = sessionDO.getStopLossPrice();
        this.status = sessionDO.getStatus();
        this.createTime = sessionDO.getCreateTime();
        this.updateTime = sessionDO.getUpdateTime();
        this.remark = sessionDO.getRemark();

        // 计算净盈亏
        this.netPnl = this.closePnl.subtract(this.openFee).subtract(this.closeFee);

        // 设置状态名称
        SessionStatusEnum statusEnum = SessionStatusEnum.byCode(sessionDO.getStatus());
        this.statusName = statusEnum != null ? statusEnum.getMessage() : "未知";
    }
}
