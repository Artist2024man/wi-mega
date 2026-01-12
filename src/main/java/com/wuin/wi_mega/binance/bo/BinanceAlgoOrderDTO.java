package com.wuin.wi_mega.binance.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BinanceAlgoOrderDTO {

    /** 算法单 ID */
    private Long algoId;

    /** 客户端自定义算法单 ID */
    private String clientAlgoId;

    /** 算法类型：CONDITIONAL */
    private String algoType;

    /** 订单类型：TAKE_PROFIT / STOP / TRAILING_STOP_MARKET 等 */
    private String orderType;

    private String symbol;

    /** BUY / SELL */
    private String side;

    /** BOTH / LONG / SHORT */
    private String positionSide;

    /** GTC / IOC / FOK */
    private String timeInForce;

    /** 下单数量 */
    private String quantity;

    /** NEW / WORKING / CANCELED / EXPIRED 等 */
    private String algoStatus;

    /** 触发价格 */
    private BigDecimal triggerPrice;

    /** 委托价格 */
    private BigDecimal price;

    /** 冰山数量（可能为 null） */
    private BigDecimal icebergQuantity;

    /** 自成交保护模式 */
    private String selfTradePreventionMode;

    /** CONTRACT_PRICE / MARK_PRICE */
    private String workingType;

    /** 价格撮合方式 */
    private String priceMatch;

    /** 是否平仓 */
    private Boolean closePosition;

    /** 是否开启价格保护 */
    private Boolean priceProtect;

    /** 是否只减仓 */
    private Boolean reduceOnly;

    /**
     * 激活价格
     * 仅 TRAILING_STOP_MARKET 订单有值
     */
    private BigDecimal activatePrice;

    /**
     * 回调率
     * 仅 TRAILING_STOP_MARKET 订单有值
     */
    private String callbackRate;

    /** 创建时间（毫秒） */
    private Long createTime;

    /** 更新时间（毫秒） */
    private Long updateTime;

    /** 触发时间（毫秒，未触发为 0） */
    private Long triggerTime;

    /** 到期时间（毫秒） */
    private Long goodTillDate;
}