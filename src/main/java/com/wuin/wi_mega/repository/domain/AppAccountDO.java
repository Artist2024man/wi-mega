package com.wuin.wi_mega.repository.domain;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuin.wi_mega.binance.bo.*;
import com.wuin.wi_mega.binance.trade.AccountTradeClient;
import com.wuin.wi_mega.common.enums.TradeTypeEnum;
import com.wuin.wi_mega.common.exception.APIRuntimeException;
import com.wuin.wi_mega.repository.base.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@TableName("app_account")
public class AppAccountDO extends BaseDO {

    private static final String WS_BASE = "wss://fstream.binance.com/ws/";

    private static final String REST_BASE = "https://fapi.binance.com";

    private static final Long KEEPALIVE_INTERVAL = 30L;

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "账户名称")
    private String name;

    @Schema(description = "KEY")
    private String apiKey;

    @Schema(description = "密码")
    private String apiKeyPass;

    @Schema(description = "所属平台")
    private String exchange;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "初始净值")
    private BigDecimal initEquity;

    @Schema(description = "当前净值")
    private BigDecimal curEquity;

    @Schema(description = "净值单位，支持：USDT,USDC,USD,USD1")
    private String equityCoin;

    @Schema(description = "下次同步净值时间")
    private LocalDateTime nextSyncTime;

    @Schema(description = "策略实例ID")
    private Long strategyInstanceId;

    @Schema(description = "策略状态")
    private Integer strategyStatus;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "持仓模式：1=单向持仓，2=双向持仓")
    private Integer dualSidePosition;

    @Schema(description = "杠杆倍数，支持:1-125")
    private Integer leverage;

    @Schema(description = "交易客户端")
    @TableField(exist = false)
    private AccountTradeClient tradeClient;

    @Schema(description = "盈亏")
    private BigDecimal closePnl;

    @Schema(description = "开仓手续费")
    private BigDecimal openFee;

    @Schema(description = "完成手续费")
    private BigDecimal closeFee;

    @Schema(description = "最后一个同步会话的ID")
    private Long lastSyncSessionId;

    @Schema(description = "交易模式：1=模拟，2=实仓")
    private Integer tradeType;

    @Schema(description = "挂单手续费费率")
    private BigDecimal makerFeeRate;

    @Schema(description = "吃单手续费费率")
    private BigDecimal takerFeeRate;

    @Schema(description = "策略执行允许的最小价格")
    private BigDecimal strategyMinPrice;

    @Schema(description = "策略执行允许的最大价格")
    private BigDecimal strategyMaxPrice;

    public BinanceOrderDTO openLongLimit(String clientOrderId, BigDecimal price, BigDecimal qty) {
        // 双向持仓：多头下单使用 positionSide=LONG
        String resp = getTradeClient().openLong(clientOrderId, this.symbol, price, qty, "GTC");
        return JSON.parseObject(resp, BinanceOrderDTO.class);
    }

    public BinanceOrderDTO openShortLimit(String clientOrderId, BigDecimal price, BigDecimal qty) {
        // 双向持仓：空头下单使用 positionSide=SHORT
        String resp = getTradeClient().openShort(clientOrderId, this.symbol, price, qty, "GTC");
        return JSON.parseObject(resp, BinanceOrderDTO.class);
    }

    public BinanceOrderDTO marketOpenLong(String clientOrderId, BigDecimal qty) {
        String resp = getTradeClient().marketOpenLong(clientOrderId, this.symbol, qty);
        return JSON.parseObject(resp, BinanceOrderDTO.class);
    }

    public BinanceOrderDTO closeLong(String clientOrderId, BigDecimal qty) {
        String resp = getTradeClient().closeLong(clientOrderId, symbol, qty);
        return JSON.parseObject(resp, BinanceOrderDTO.class);
    }

    public BinanceOrderDTO closeShort(String clientOrderId, BigDecimal qty) {
        String resp = getTradeClient().closeShort(clientOrderId, symbol, qty);
        return JSON.parseObject(resp, BinanceOrderDTO.class);
    }

    /**
     * 市价开空
     */
    public BinanceOrderDTO marketOpenShort(String clientOrderId, BigDecimal qty) {
        String resp = getTradeClient().marketOpenShort(clientOrderId, this.symbol, qty);
        return JSON.parseObject(resp, BinanceOrderDTO.class);
    }

    public BinanceOrderDTO queryOrder(Long orderId, String clientOrderId) {
        try {
            String resp = getTradeClient().queryOrder(this.symbol, orderId, clientOrderId);
            return JSON.parseObject(resp, BinanceOrderDTO.class);
        } catch (APIRuntimeException e) {
            if (JSON.isValid(e.getMessage())) {
                JSONObject obj = JSON.parseObject(e.getMessage());
                if (obj.getInteger("code").compareTo(-2013) == 0) {
                    return null;
                }
            }
            throw e;
        }
    }

    public BinanceOrderDTO queryOpenOrder(Long orderId, String clientOrderId) {
        try {
            String resp = getTradeClient().queryOpenOrder(this.symbol, orderId, clientOrderId);
            return JSON.parseObject(resp, BinanceOrderDTO.class);
        } catch (APIRuntimeException e) {
            if (JSON.isValid(e.getMessage())) {
                JSONObject obj = JSON.parseObject(e.getMessage());
                if (obj.getInteger("code").compareTo(-2013) == 0) {
                    return null;
                }
            }
            throw e;
        }
    }

    public BinanceAlgoOrderDTO stopLoss(boolean longPosition, BigDecimal stopPrice) {
        String positionSide = longPosition ? "LONG" : "SHORT";
        String resp = getTradeClient().stopLoss(this.symbol, positionSide, stopPrice);
        return JSON.parseObject(resp, BinanceAlgoOrderDTO.class);
    }

    public BinanceAlgoOrderDTO takeProfit(boolean longPosition, BigDecimal stopPrice) {
        String positionSide = longPosition ? "LONG" : "SHORT";
        String resp = getTradeClient().takeProfit(this.symbol, positionSide, stopPrice);
        return JSON.parseObject(resp, BinanceAlgoOrderDTO.class);
    }

    public String cancel(Long orderId, String clientOrderId) {
        return getTradeClient().cancelOrder(this.symbol, orderId, clientOrderId);
    }

    public String cancelAllAlgoOrders() {
        return getTradeClient().cancelAllAlgoOrders(this.symbol);
    }

    public String cancelAlgoOrders(String clientAlgoId, Long algoId) {
        return getTradeClient().cancelAlgoOrders(this.symbol, clientAlgoId, algoId);
    }

    public List<BinancePosition> positionRisk() {
        String resp = getTradeClient().positionRiskV3(this.symbol);
        return JSON.parseArray(resp, BinancePosition.class);
    }

    public List<FuturesAccountAssetDTO> fetchBalanceV3() {
        String resp = getTradeClient().fetchBalanceV3();
        return JSON.parseArray(resp, FuturesAccountAssetDTO.class);
    }

    public List<FuturesTradeDTO> trades(Long orderId, Long fromId, int limit) {
        String resp = getTradeClient().userTrades(this.symbol, orderId, fromId, limit, null, null);
        return StringUtils.isBlank(resp) ? new ArrayList<>() : JSON.parseArray(resp, FuturesTradeDTO.class);
    }

    public String fetchOpenOrders() {
        return getTradeClient().openOrders(this.symbol);
    }

    /**
     * 更改持仓模式：dualSidePosition=true 双向；false 单向
     * POST /fapi/v1/positionSide/dual
     */
    public String changePositionMode(boolean dualSide) {
        return getTradeClient().changePositionMode(dualSide);
    }

    /**
     * 查询当前持仓模式 GET /fapi/v1/positionSide/dual
     */
    public Boolean fetchDualSidePosition() {
        String resp = getTradeClient().isDualSidePosition();
        JSONObject resJSON = JSON.parseObject(resp);
        return resJSON.getBoolean("dualSidePosition");
    }

    public BinanceCommissionRateDTO feeRate() {
        String resp = getTradeClient().feeRate(this.symbol);
        return JSON.parseObject(resp, BinanceCommissionRateDTO.class);
    }

    public String fetchAccountInfoV3() {
        return getTradeClient().fetchAccountInfoV3();
    }

    public List<BinanceAlgoOrderDTO> openAlgoOrders() {
        String resp = getTradeClient().algoOrders(this.symbol);
        return JSON.parseArray(resp, BinanceAlgoOrderDTO.class);
    }

    public BinanceOrderDTO openMarket(String clientOrderId, String position, BigDecimal qty, BigDecimal expectPrice) {
        try {
            if (TradeTypeEnum.MOCK.equalsByCode(this.tradeType)) {
                String side = position.equals("LONG") ? "BUY" : "SELL";
                BinanceOrderDTO orderDTO = BinanceOrderDTO.mock(clientOrderId, this.symbol, expectPrice, position, side, qty);
                log.warn("openMarket -> mockData accountId={}, clientOrderId={}, position={}, qty={}", this.getId(), clientOrderId, position, qty);
                return orderDTO;
            }
            log.warn("openMarket -> accountId={}, clientOrderId={}, position={}, qty={}", this.getId(), clientOrderId, position, qty);
            if (position.equals("LONG")) {
                return marketOpenLong(clientOrderId, qty);
            } else {
                return marketOpenShort(clientOrderId, qty);
            }
        } catch (Throwable t) {
            log.error("openOrder error, accountId=" + this.getId() + ", clientOrderId=" + clientOrderId, t);
            return null;
        }
    }

    public BinanceOrderDTO closeMarket(String clientOrderId, String position, BigDecimal qty, BigDecimal expectPrice) {
        try {
            if (TradeTypeEnum.MOCK.equalsByCode(this.tradeType)) {
                String side = position.equals("LONG") ? "SELL" : "BUY";
                BinanceOrderDTO orderDTO = BinanceOrderDTO.mock(clientOrderId, this.symbol, expectPrice, position, side, qty);
                log.warn("closeMarket -> mockData accountId={}, clientOrderId={}, position={}, qty={}", this.getId(), clientOrderId, position, qty);
                return orderDTO;
            }
            log.warn("closeMarket -> accountId={}, clientOrderId={}, position={}, qty={}", this.getId(), clientOrderId, position, qty);
            if (position.equals("LONG")) {
                return closeLong(clientOrderId, qty);
            } else {
                return closeShort(clientOrderId, qty);
            }
        } catch (Throwable t) {
            log.error("openOrder error, accountId=" + this.getId() + ", clientOrderId=" + clientOrderId, t);
            return null;
        }
    }

    /**
     * 调整杠杆 POST /fapi/v1/leverage
     */
    public String changeLeverage(int leverage) {
        return getTradeClient().changeLeverage(this.symbol, leverage);
    }

    public AccountTradeClient getTradeClient() {
        if (tradeClient == null) {
            synchronized (AccountTradeClient.class) {
                if (tradeClient == null) {
                    tradeClient = new AccountTradeClient(apiKey, apiKeyPass);
                }
            }
        }
        return tradeClient;
    }

    /**
     * 判断是否为模拟交易模式
     */
    public boolean isMockMode() {
        return TradeTypeEnum.MOCK.equalsByCode(this.tradeType);
    }
}
