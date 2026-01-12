package com.wuin.wi_mega.repository.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wuin.wi_mega.binance.bo.BinanceOrderDTO;
import com.wuin.wi_mega.common.enums.*;
import com.wuin.wi_mega.repository.base.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("app_account_order")
@Schema(description = "账户交易订单表")
@NoArgsConstructor
public class AppAccountOrderDO extends BaseDO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "账号ID")
    private Long accountId;

    @Schema(description = "策略实例ID")
    private Long strategyInstanceId;

    @Schema(description = "交易平台")
    private String exchange;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "订单类型：1=开仓，2=平仓")
    private Integer orderType;

    @Schema(description = "订单ID")
    private String clientOrderId;

    @Schema(description = "期望价格")
    private BigDecimal expectPrice;

    @Schema(description = "数量")
    private BigDecimal qty;

    @Schema(description = "持仓方向：LONG=多，SHORT=空")
    private String positionSide;

    @Schema(description = "买卖方向：BUY=买入，SELL=卖出")
    private String buySide;

    @Schema(description = "平仓盈亏")
    private BigDecimal closePnl = BigDecimal.ZERO;

    @Schema(description = "手续费")
    private BigDecimal fee = BigDecimal.ZERO;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "最终数据同步状态：1=完成，0=未完成")
    private Integer syncStatus;

    @Schema(description = "下次执行时间")
    private LocalDateTime nextCheckTime;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "实际成交价格")
    private BigDecimal avePrice;

    @Schema(description = "成交金额")
    private BigDecimal cumQuote;

    @Schema(description = "是否模拟数据：1=是，0=否")
    private Integer mockData;

    public AppAccountOrderDO(AppAccountDO accountDO, Long sessionId, BinanceOrderDTO orderDTO, AccountOrderTypeEnum orderType,
                             String positionSide, String buySide, String clientOrderId, BigDecimal expectPrice, BigDecimal qty) {
        this.userId = accountDO.getUserId();
        this.accountId = accountDO.getId();
        this.strategyInstanceId = accountDO.getStrategyInstanceId();
        this.symbol = accountDO.getSymbol();
        this.orderType = orderType.code();
        this.exchange = accountDO.getExchange();
        this.expectPrice = expectPrice;
        this.clientOrderId = clientOrderId;
        this.qty = qty;
        this.positionSide = positionSide;
        this.buySide = buySide;
        this.closePnl = BigDecimal.ZERO;
        this.fee = BigDecimal.ZERO;
        this.status = BaOrderStatusEnum.NEW.code();
        this.syncStatus = SyncStatusEnum.NO_SYNC.code();
        this.nextCheckTime = LocalDateTime.now().plusSeconds(2); //两秒后进行确认
        this.avePrice = expectPrice; //默认等于期望价格
        this.mockData = TradeTypeEnum.MOCK.equalsByCode(accountDO.getTradeType()) ? MockDataEnum.MOCK.code() : MockDataEnum.REAL.code();

        String remark = "创建订单";
        if (null != sessionId) {
            this.sessionId = sessionId;
        } else {
            remark = remark.concat(",没有会话对应关系");
        }

        if (null != orderDTO) {
            this.orderId = orderDTO.getOrderId();
            this.avePrice = orderDTO.getAvgPrice();
            this.cumQuote = orderDTO.getCumQuote();
        }
        this.remark = remark;
    }
}
