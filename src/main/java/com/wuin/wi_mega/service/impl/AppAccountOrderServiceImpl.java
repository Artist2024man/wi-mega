package com.wuin.wi_mega.service.impl;

import com.wuin.wi_mega.binance.bo.BinanceOrderDTO;
import com.wuin.wi_mega.binance.bo.BinancePosition;
import com.wuin.wi_mega.common.enums.AccountOrderTypeEnum;
import com.wuin.wi_mega.common.enums.BaOrderStatusEnum;
import com.wuin.wi_mega.common.enums.MockDataEnum;
import com.wuin.wi_mega.common.enums.TradeTypeEnum;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import com.wuin.wi_mega.repository.AppAccountOrderRepository;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppAccountOrderDO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.service.AppAccountOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class AppAccountOrderServiceImpl implements AppAccountOrderService {

    @Autowired
    private AppAccountOrderRepository appAccountOrderRepository;

    @Override
    public AppAccountOrderDO openMarket(Long id, AppAccountDO accountDO, Long sessionId, String position, BigDecimal expectPrice, BigDecimal qty) {
        AppAccountOrderDO orderDO;
        String clientOrderId = "OM_" + SimpleSnowflake.nextId();
        if (null != id) {
            orderDO = appAccountOrderRepository.getById(id);
            if (null != orderDO) {
                BinanceOrderDTO orderDTO = accountDO.queryOrder(orderDO.getOrderId(), orderDO.getClientOrderId());
                if (null != orderDTO) {
                    this.updateOrderInfo(orderDO, orderDTO, null);
                    return orderDO;
                }
                clientOrderId = orderDO.getClientOrderId();
            } else {
                throw new IllegalArgumentException("无效的订单ID");
            }
        } else {
            String buySide = "LONG".equals(position) ? "BUY" : "SELL";
            orderDO = new AppAccountOrderDO(accountDO, sessionId, null, AccountOrderTypeEnum.OPEN,
                    position, buySide, clientOrderId, expectPrice, qty);
            appAccountOrderRepository.save(orderDO);
        }
        BinanceOrderDTO orderDTO = accountDO.openMarket(clientOrderId, position, qty, expectPrice);
        if (null != orderDTO) {
            this.updateOrderInfo(orderDO, orderDTO, qty);
        }
        return orderDO;
    }

    @Override
    public AppAccountOrderDO closeMarket(Long id, AppAccountDO accountDO, AppAccountSessionDO sessionDO, String position, BigDecimal expectPrice, BigDecimal qty) {
        Long sessionId = null == sessionDO ? -1L : sessionDO.getId();
        log.warn("closeMarket -> try close position, sessionId={}, position={}, qty={}", sessionId, position, qty);
        AppAccountOrderDO orderDO;
        String clientOrderId = "CM_" + SimpleSnowflake.nextId();
        BigDecimal closeQty = qty;

        if (null != id) {
            orderDO = appAccountOrderRepository.getById(id);
            if (null != orderDO && MockDataEnum.REAL.equalsByCode(orderDO.getMockData())) { //真实数据才查询持仓
                BinanceOrderDTO orderDTO = accountDO.queryOrder(orderDO.getOrderId(), orderDO.getClientOrderId());
                if (null != orderDTO) {
                    this.updateOrderInfo(orderDO, orderDTO, null);
                    return orderDO;
                }
                clientOrderId = orderDO.getClientOrderId();
                closeQty = orderDO.getQty();
            } else {
                log.warn("closeMarket -> 无效的订单ID, sessionId={}", sessionId);
                return null;
            }
        } else {
            String buySide = "LONG".equals(position) ? "SELL" : "BUY";
            if (TradeTypeEnum.REAL.equalsByCode(accountDO.getTradeType())) {
                if (null == closeQty) {
                    List<BinancePosition> positionList = accountDO.positionRisk();
                    if (positionList == null || positionList.isEmpty()) {
                        log.warn("closeMarket -> positionRisk is null or empty, sessionId={}, position={}", sessionId, position);
                        return null;
                    }
                    for (BinancePosition p : positionList) {
                        if (p.getPositionSide().equals(position)) {
                            closeQty = p.getPositionAmt().abs();
                            log.warn("closeMarket -> find position, sessionId={}, position={}, qty={}", sessionId, position, closeQty);
                            break;
                        }
                    }
                    if (null == closeQty || closeQty.compareTo(BigDecimal.ZERO) <= 0) {
                        log.warn("closeMarket -> not find position or invalid closeQty accountId={}, sessionId={}, position={}, closeQty={}",
                                accountDO.getId(), sessionId, position, closeQty == null ? "null" : closeQty );
                        return null;
                    }
                }
            } else {
                closeQty = null == sessionDO ? qty : sessionDO.getHoldQty();
            }
            orderDO = new AppAccountOrderDO(accountDO, sessionId, null, AccountOrderTypeEnum.CLOSE,
                    position, buySide, clientOrderId, expectPrice, closeQty);
            appAccountOrderRepository.save(orderDO);
        }
        BinanceOrderDTO orderDTO = accountDO.closeMarket(clientOrderId, position, closeQty, expectPrice);
        if (null != orderDTO) {
            this.updateOrderInfo(orderDO, orderDTO, closeQty);
        }
        return orderDO;
    }

    private void updateOrderInfo(AppAccountOrderDO orderDO, BinanceOrderDTO orderDTO, BigDecimal closeQty){
        AppAccountOrderDO updated = new AppAccountOrderDO();
        updated.setId(orderDO.getId());
        updated.setOrderId(orderDTO.getOrderId());
        updated.setAvePrice(orderDTO.getAvgPrice());
        updated.setCumQuote(orderDO.getCumQuote());
        updated.setStatus(BaOrderStatusEnum.valueOf(orderDTO.getStatus()).code());
        updated.setMockData(orderDTO.isMock() ? MockDataEnum.MOCK.code() : MockDataEnum.REAL.code());
        if (null != closeQty) {
            updated.setQty(closeQty);
        }
        appAccountOrderRepository.updateById(updated);
        orderDO.setOrderId(orderDTO.getOrderId());
        orderDO.setAvePrice(orderDTO.getAvgPrice());
        orderDO.setCumQuote(orderDO.getCumQuote());
        orderDO.setMockData(orderDTO.isMock() ? MockDataEnum.MOCK.code() : MockDataEnum.REAL.code());
    }
}
