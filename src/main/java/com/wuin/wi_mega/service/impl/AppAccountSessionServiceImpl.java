package com.wuin.wi_mega.service.impl;

import com.wuin.wi_mega.binance.bo.FuturesTradeDTO;
import com.wuin.wi_mega.common.cache.redis.BinanceAccountLastLogIdCache;
import com.wuin.wi_mega.common.enums.AccountOrderTypeEnum;
import com.wuin.wi_mega.common.enums.SessionStatusEnum;
import com.wuin.wi_mega.common.enums.SyncStatusEnum;
import com.wuin.wi_mega.common.enums.TradeTypeEnum;
import com.wuin.wi_mega.repository.AppAccountOrderRepository;
import com.wuin.wi_mega.repository.AppAccountRepository;
import com.wuin.wi_mega.repository.AppAccountSessionRepository;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppAccountOrderDO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.service.AppAccountSessionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AppAccountSessionServiceImpl implements AppAccountSessionService {

    @Autowired
    private AppAccountSessionRepository appAccountSessionRepository;

    @Autowired
    private AppAccountRepository appAccountRepository;

    @Autowired
    private BinanceAccountLastLogIdCache binanceAccountLastLogIdCache;

    @Autowired
    private AppAccountOrderRepository appAccountOrderRepository;

    public List<AppAccountSessionDO> listOpeningByAccountIds(Collection<Long> accountIds) {
        return appAccountSessionRepository.listByAccountIdsAndStatusList(accountIds, SessionStatusEnum.running());
    }

    @Override
    public List<AppAccountSessionDO> listNeedSync() {
        return appAccountSessionRepository.listNeedSyncByStatusList(SessionStatusEnum.completed(), LocalDateTime.now().minusMinutes(120)); //只查询最近2小时内更新的数据
    }

    @Override
    public void doFinalSync(AppAccountSessionDO sessionDO) {
        AppAccountDO accountDO = appAccountRepository.getById(sessionDO.getAccountId());
        AppAccountSessionDO updateSession = new AppAccountSessionDO();
        updateSession.setId(sessionDO.getId());
        updateSession.setSyncStatus(SyncStatusEnum.FINISHED.getCode());
        if (TradeTypeEnum.REAL.equalsByCode(accountDO.getTradeType())) {
            //同步基础盈亏/手续费数据
            this.syncAccountOrder(accountDO, sessionDO.getId());

            List<AppAccountOrderDO> orderList = appAccountOrderRepository.listBySessionId(sessionDO.getId());
            if (CollectionUtils.isEmpty(orderList)) {
                updateSession.setRemark("当前会话没有对应的交易日志");
            } else {
                BigDecimal openFee = BigDecimal.ZERO;
                BigDecimal closeFee = BigDecimal.ZERO;
                BigDecimal closePnl = BigDecimal.ZERO;
                for (AppAccountOrderDO orderDO : orderList) {
                    if (AccountOrderTypeEnum.OPEN.equalsByCode(orderDO.getOrderType())) {
                        openFee = openFee.add(orderDO.getFee());
                    } else {
                        closeFee = closeFee.add(orderDO.getFee());
                        closePnl = closePnl.add(orderDO.getClosePnl());
                    }
                }
                updateSession.setOpenFee(openFee);
                updateSession.setCloseFee(closeFee);
                updateSession.setClosePnl(closePnl);
            }
        }
        appAccountSessionRepository.updateById(updateSession);
        log.warn("doFinalSync -> completed, sessionId={}", updateSession.getId());
    }

    private void syncAccountOrder(AppAccountDO accountDO, Long sessionId) {
        Long fromId = binanceAccountLastLogIdCache.get(accountDO.getId().toString());

        List<FuturesTradeDTO> tradeList = accountDO.trades(null, fromId, 300);
        if (CollectionUtils.isEmpty(tradeList)) {
            log.warn("doFinalSync -> 当前没有任何新增的的交易日志，accountId:{}, sessionId={}", accountDO.getId(), sessionId);
            return;
        }

        Set<Long> orderIdSet = tradeList.stream().map(FuturesTradeDTO::getOrderId).collect(Collectors.toSet());

        List<AppAccountOrderDO> orderList = appAccountOrderRepository.listByOrderIds(accountDO.getId(), orderIdSet);
        if (CollectionUtils.isEmpty(orderList)) {
            log.warn("doFinalSync -> 用户账号交易记录没有任何对应的数据，sessionId:{}, accountId={}, orderIds={}", sessionId, accountDO.getId(), orderIdSet);
            return;
        }

        Map<Long, List<FuturesTradeDTO>> tradeMap = tradeList.stream().collect(Collectors.groupingBy(FuturesTradeDTO::getOrderId));

        List<AppAccountOrderDO> updateList = new ArrayList<>();

        for (AppAccountOrderDO orderDO : orderList) {
            List<FuturesTradeDTO> innList = tradeMap.get(orderDO.getOrderId());
            if (CollectionUtils.isEmpty(innList)) {
                continue;
            }
            AppAccountOrderDO update = new AppAccountOrderDO();
            update.setId(orderDO.getId());
            for (FuturesTradeDTO inn  : innList) {
                update.setFee(update.getFee().add(inn.getCommission()));
                update.setClosePnl(update.getClosePnl().add(inn.getRealizedPnl()));
            }
            updateList.add(update);
        }

        if (CollectionUtils.isNotEmpty(updateList)) {
            appAccountOrderRepository.updateBatchById(updateList);
        }
        //保存当前已经查询的最大ID
        Long maxId = Math.max(tradeList.getFirst().getId(), tradeList.getLast().getId());
        binanceAccountLastLogIdCache.put(accountDO.getId().toString(), maxId);
    }


}
