package com.wuin.wi_mega.model.vo;

import com.wuin.wi_mega.common.enums.AccountOrderTypeEnum;
import com.wuin.wi_mega.common.enums.BaOrderStatusEnum;
import com.wuin.wi_mega.repository.domain.AppAccountOrderDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "订单历史记录")
public class OrderHistoryVO {

    @Schema(description = "订单ID（系统）")
    private Long id;

    @Schema(description = "账号ID")
    private Long accountId;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "策略实例ID")
    private Long strategyInstanceId;

    @Schema(description = "交易平台")
    private String exchange;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "订单类型码：1=开仓，2=平仓")
    private Integer orderType;

    @Schema(description = "订单类型名称")
    private String orderTypeName;

    @Schema(description = "客户端订单ID")
    private String clientOrderId;

    @Schema(description = "交易所订单ID")
    private Long orderId;

    @Schema(description = "期望价格")
    private BigDecimal expectPrice;

    @Schema(description = "实际成交均价")
    private BigDecimal avePrice;

    @Schema(description = "成交数量")
    private BigDecimal qty;

    @Schema(description = "成交金额")
    private BigDecimal cumQuote;

    @Schema(description = "持仓方向：LONG=多，SHORT=空")
    private String positionSide;

    @Schema(description = "买卖方向：BUY=买入，SELL=卖出")
    private String buySide;

    @Schema(description = "平仓盈亏")
    private BigDecimal closePnl;

    @Schema(description = "手续费")
    private BigDecimal fee;

    @Schema(description = "状态码")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "滑点（实际价格与期望价格的差异百分比）")
    private BigDecimal slippage;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;

    public OrderHistoryVO() {
    }

    public OrderHistoryVO(AppAccountOrderDO orderDO) {
        this.id = orderDO.getId();
        this.accountId = orderDO.getAccountId();
        this.sessionId = orderDO.getSessionId();
        this.strategyInstanceId = orderDO.getStrategyInstanceId();
        this.exchange = orderDO.getExchange();
        this.symbol = orderDO.getSymbol();
        this.orderType = orderDO.getOrderType();
        this.clientOrderId = orderDO.getClientOrderId();
        this.orderId = orderDO.getOrderId();
        this.expectPrice = orderDO.getExpectPrice();
        this.avePrice = orderDO.getAvePrice();
        this.qty = orderDO.getQty();
        this.cumQuote = orderDO.getCumQuote();
        this.positionSide = orderDO.getPositionSide();
        this.buySide = orderDO.getBuySide();
        this.closePnl = orderDO.getClosePnl() != null ? orderDO.getClosePnl() : BigDecimal.ZERO;
        this.fee = orderDO.getFee() != null ? orderDO.getFee() : BigDecimal.ZERO;
        this.status = orderDO.getStatus();
        this.createTime = orderDO.getCreateTime();
        this.updateTime = orderDO.getUpdateTime();
        this.remark = orderDO.getRemark();

        // 设置订单类型名称
        AccountOrderTypeEnum orderTypeEnum = AccountOrderTypeEnum.byCode(orderDO.getOrderType());
        this.orderTypeName = orderTypeEnum != null ? orderTypeEnum.getMessage() : "未知";

        // 设置状态名称
        BaOrderStatusEnum statusEnum = BaOrderStatusEnum.byCode(orderDO.getStatus());
        this.statusName = statusEnum != null ? statusEnum.getMessage() : "未知";

        // 计算滑点
        if (this.expectPrice != null && this.avePrice != null
                && this.expectPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = this.avePrice.subtract(this.expectPrice).abs();
            this.slippage = diff.divide(this.expectPrice, 6, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
        } else {
            this.slippage = BigDecimal.ZERO;
        }
    }
}
