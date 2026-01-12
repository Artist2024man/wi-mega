package com.wuin.wi_mega.binance.bo;

import com.wuin.wi_mega.common.enums.BaOrderStatusEnum;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BinanceOrderDTO {

    private Long orderId;

    private String symbol;

    /**
     * NEW / PARTIALLY_FILLED / FILLED / CANCELED 等
     */
    private String status;

    private String clientOrderId;

    /**
     * 下单价格
     */
    private BigDecimal price;

    /**
     * 成交均价
     */
    private BigDecimal avgPrice;

    /**
     * 原始下单数量
     */
    private BigDecimal origQty;

    /**
     * 已成交数量
     */
    private BigDecimal executedQty;

    /**
     * 累计成交数量
     */
    private BigDecimal cumQty;

    /**
     * 累计成交金额
     */
    private BigDecimal cumQuote;

    /**
     * GTC / IOC / FOK
     */
    private String timeInForce;

    /**
     * LIMIT / MARKET / STOP / TAKE_PROFIT 等
     */
    private String type;

    /**
     * 是否只减仓
     */
    private Boolean reduceOnly;

    /**
     * 是否平仓
     */
    private Boolean closePosition;

    /**
     * BUY / SELL
     */
    private String side;

    /**
     * LONG / SHORT
     */
    private String positionSide;

    /**
     * 止损/止盈触发价
     */
    private BigDecimal stopPrice;

    /**
     * CONTRACT_PRICE / MARK_PRICE
     */
    private String workingType;

    /**
     * 是否开启价格保护
     */
    private Boolean priceProtect;

    /**
     * 原始订单类型
     */
    private String origType;

    /**
     * 价格撮合方式
     */
    private String priceMatch;

    /**
     * 自成交保护模式
     */
    private String selfTradePreventionMode;

    /**
     * 到期时间（毫秒）
     */
    private Long goodTillDate;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updateTime;

    /**
     * 是否模拟数据
     */
    private Boolean mockData;

    public static BinanceOrderDTO mock(String clientOrderId, String symbol, BigDecimal price, String positionSide, String buySide, BigDecimal qty) {
        BinanceOrderDTO dto = new BinanceOrderDTO();
        dto.orderId = SimpleSnowflake.nextId();
        dto.symbol = symbol;
        dto.status = BaOrderStatusEnum.FILLED.name();
        dto.clientOrderId = clientOrderId;
        dto.price = price;
        dto.avgPrice = price;
        dto.origQty = qty;
        dto.executedQty = qty;
        dto.positionSide = positionSide;
        dto.side = buySide;
        dto.mockData = true;
        return dto;
    }

    public boolean isMock() {
        return null != mockData && mockData;
    }
}