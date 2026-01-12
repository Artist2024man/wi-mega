package com.wuin.wi_mega.service;

import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppAccountOrderDO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;

import java.math.BigDecimal;

public interface AppAccountOrderService {

    /**
     * 市价开仓
     *
     * @param accountDO   账号信息
     * @param sessionId   会话ID
     * @param position    方向：LONG=开多，SHORT=开空
     * @param expectPrice 期望价格
     * @param qty         交易数量
     * @return 当前订单信息
     */
    AppAccountOrderDO openMarket(Long id, AppAccountDO accountDO, Long sessionId, String position, BigDecimal expectPrice, BigDecimal qty);

    /**
     * 市价平仓
     *
     * @param accountDO   账号信息
     * @param sessionDO   会话
     * @param position    方向：LONG=平多，SHORT=平空
     * @param expectPrice 期望价格
     * @param qty         交易数量 (暂时不使用)
     * @return 当前订单信息
     */
    AppAccountOrderDO closeMarket(Long id, AppAccountDO accountDO, AppAccountSessionDO sessionDO, String position, BigDecimal expectPrice, BigDecimal qty);




}
