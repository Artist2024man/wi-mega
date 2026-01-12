package com.wuin.wi_mega.model.vo;

import com.wuin.wi_mega.binance.bo.BinancePosition;
import com.wuin.wi_mega.common.enums.SessionStatusEnum;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "会话详情")
public class SessionDetailVO {

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

    @Schema(description = "持仓价值")
    private BigDecimal holdValue;

    @Schema(description = "当前价格")
    private BigDecimal currentPrice;

    @Schema(description = "未实现盈亏")
    private BigDecimal unrealizedPnl;

    @Schema(description = "未实现盈亏百分比")
    private BigDecimal unrealizedPnlPercent;

    @Schema(description = "已实现盈亏")
    private BigDecimal closePnl;

    @Schema(description = "开仓手续费")
    private BigDecimal openFee;

    @Schema(description = "平仓手续费")
    private BigDecimal closeFee;

    @Schema(description = "止盈价格")
    private BigDecimal takeProfitPrice;

    @Schema(description = "止损价格")
    private BigDecimal stopLossPrice;

    @Schema(description = "状态码")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "业务状态")
    private Integer bizStatus;

    @Schema(description = "持仓方向")
    private String positionSide;

    @Schema(description = "持仓列表")
    private List<BinancePosition> positions;

    @Schema(description = "基础参数(JSON)")
    private String baseParam;

    @Schema(description = "运行参数(JSON)")
    private String runParam;

    @Schema(description = "业务参数(JSON)")
    private String bizParam;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;

    public SessionDetailVO() {
    }

    public SessionDetailVO(AppAccountSessionDO sessionDO) {
        this.id = sessionDO.getId();
        this.accountId = sessionDO.getAccountId();
        this.strategyInstanceId = sessionDO.getStrategyInstanceId();
        this.strategyCode = sessionDO.getStrategyCode();
        this.exchange = sessionDO.getExchange();
        this.symbol = sessionDO.getSymbol();
        this.holdAvePrice = sessionDO.getHoldAvePrice();
        this.holdQty = sessionDO.getHoldQty();
        this.closePnl = sessionDO.getClosePnl();
        this.openFee = sessionDO.getOpenFee();
        this.closeFee = sessionDO.getCloseFee();
        this.takeProfitPrice = sessionDO.getTakeProfitPrice();
        this.stopLossPrice = sessionDO.getStopLossPrice();
        this.status = sessionDO.getStatus();
        this.bizStatus = sessionDO.getBizStatus();
        this.baseParam = sessionDO.getBaseParam();
        this.runParam = sessionDO.getRunParam();
        this.bizParam = sessionDO.getBizParam();
        this.createTime = sessionDO.getCreateTime();
        this.updateTime = sessionDO.getUpdateTime();
        this.remark = sessionDO.getRemark();

        // 设置状态名称
        SessionStatusEnum statusEnum = SessionStatusEnum.byCode(sessionDO.getStatus());
        this.statusName = statusEnum != null ? statusEnum.getMessage() : "未知";
    }
}
