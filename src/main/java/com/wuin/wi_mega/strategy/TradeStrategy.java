package com.wuin.wi_mega.strategy;

import com.alibaba.fastjson2.JSON;
import com.wuin.wi_mega.common.cache.local.DepthCacheFactory;
import com.wuin.wi_mega.common.cache.local.KlineCacheFactory;
import com.wuin.wi_mega.common.cache.local.StrategySignalCache;
import com.wuin.wi_mega.common.enums.*;
import com.wuin.wi_mega.common.util.LocalLock;
import com.wuin.wi_mega.model.bo.biz.StrategyBizParam;
import com.wuin.wi_mega.model.bo.signal.SessionRunningSignalBO;
import com.wuin.wi_mega.model.bo.signal.StrategyStartSignalBO;
import com.wuin.wi_mega.model.bo.base.StrategyBaseParam;
import com.wuin.wi_mega.repository.AppAccountRepository;
import com.wuin.wi_mega.repository.AppAccountSessionRepository;
import com.wuin.wi_mega.repository.AppStrategyInstanceRepository;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppAccountOrderDO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.repository.domain.AppStrategyInstanceDO;
import com.wuin.wi_mega.service.AppAccountOrderService;
import com.wuin.wi_mega.service.StrategyExecutionLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
public abstract class TradeStrategy<T extends StrategyStartSignalBO, D extends SessionRunningSignalBO> {

    @Autowired
    protected KlineCacheFactory klineCacheFactory;

    @Autowired
    protected DepthCacheFactory depthCacheFactory;

    @Autowired
    protected StrategySignalCache strategySignalCache;

    @Autowired
    protected AppAccountRepository appAccountRepository;

    @Autowired
    protected AppAccountSessionRepository appAccountSessionRepository;

    @Autowired
    protected AppStrategyInstanceRepository appStrategyInstanceRepository;

    @Autowired
    protected AppAccountOrderService appAccountOrderService;

    @Autowired
    protected StrategyExecutionLogService executionLogService;

    public T execute(AppAccountDO accountDO, AppStrategyInstanceDO instanceDO, SymbolEnum symbol) {

        if (accountDO.getStrategyMinPrice().compareTo(BigDecimal.ZERO) > 0 || accountDO.getStrategyMaxPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal curPrice = klineCacheFactory.getCurPrice(symbol, KlineIntervalEnum.MINUTE_1);
            if (curPrice.compareTo(accountDO.getStrategyMinPrice()) < 0) {
                log.warn("execute -> 当家价格超过策略设置的最小范围，停止策略，账号ID={}, 最小价格={}, 当前价格={}", accountDO.getId(), accountDO.getStrategyMinPrice(), curPrice);
                this.stopStrategy(accountDO);
                return null;
            } else if (curPrice.compareTo(accountDO.getStrategyMaxPrice()) > 0) {
                log.warn("execute -> 当家价格超过策略设置的最大范围，停止策略，账号ID={}, 最大价格={}, 当前价格={}", accountDO.getId(), accountDO.getStrategyMaxPrice(), curPrice);
                this.stopStrategy(accountDO);
                return null;
            }
        }
        T signalBO = executeInner(accountDO, instanceDO, symbol);
        return signalBO;
    }

    private void stopStrategy(AppAccountDO accountDO){
        AppAccountDO update = new AppAccountDO();
        update.setId(accountDO.getId());
        update.setStrategyStatus(StrategyStatusEnum.STOP.code());
        appAccountRepository.updateById(update);
    }

    protected abstract T executeInner(AppAccountDO accountDO, AppStrategyInstanceDO instanceDO, SymbolEnum symbol);

    public Boolean canExecute() {
        return true;
    }

    public abstract StrategyEnum strategy();

    public abstract void handlerStartSignal(T signalBO);

    public void handlerRunningSignal(AppAccountSessionDO sessionDO) {
        this.handlerRunningSignal(buildRunningSingle(sessionDO));
    }

    protected abstract void handlerRunningSignal(D signalBO);

    protected abstract D buildRunningSingle(AppAccountSessionDO sessionDO);

    protected void takeProfit(Long id, D signalBO, String position) {
        SessionBizStatusEnum statusEnum = signalBO.getStatus();
        LocalLock.executeWithoutResult("SESSION_" + id, () -> {
            log.warn("takeProfit -> 尝试止盈， sessionId={}", id);
            AppAccountSessionDO sessionDO = appAccountSessionRepository.getById(id);
            if (null == sessionDO) {
                log.warn("takeProfit -> 无效的sessionId， sessionDO={}", id);
                return;
            }
            if (SessionStatusEnum.completed().contains(sessionDO.getStatus())) {
                log.warn("takeProfit -> 当前会话状态已经完结，无需再次操作， sessionId={}, status={}", id, sessionDO.getStatus());
                return;
            }
            if (!statusEnum.equalsByCode(sessionDO.getBizStatus())) {
                log.warn("takeProfit -> 会话状态发生变化，无法继续执行， sessionId={}, expectStatus={}, sessionStatus={}",
                        id, statusEnum, SessionBizStatusEnum.byCode(sessionDO.getBizStatus()));
                return;
            }
            AppAccountDO accountDO = appAccountRepository.getById(sessionDO.getAccountId());

            // 记录止盈开始日志
            BigDecimal curPrice = klineCacheFactory.getCurPrice(signalBO.getSymbol(), KlineIntervalEnum.MINUTE_1);
            executionLogService.logSessionWithPrice(sessionDO, ExecutionLogTypeEnum.TAKE_PROFIT_START,
                    "开始止盈", String.format("会话[%d]开始执行止盈操作，当前价格=%s，止盈价格=%s",
                            id, curPrice, sessionDO.getTakeProfitPrice()),
                    curPrice, sessionDO.getTakeProfitPrice(), "SUCCESS");

            AppAccountSessionDO update = new AppAccountSessionDO();
            update.setId(sessionDO.getId());
            update.setBizStatus(SessionBizStatusEnum.TAKE_PROFIT.code());
            update.setStatus(SessionStatusEnum.TAKE_PROFIT.code());
            update.setNextCheckTime(LocalDateTime.now().plusSeconds(5));

            AppAccountOrderDO orderDO = appAccountOrderService.closeMarket(sessionDO.getTakeProfitOrderId(), accountDO, sessionDO, position, curPrice, null);
            if (null != orderDO) {
                update.setTakeProfitClientOrderId(orderDO.getClientOrderId());
                update.setTakeProfitOrderId(orderDO.getOrderId());
                if (null != orderDO.getMockData() && MockDataEnum.MOCK.equalsByCode(orderDO.getMockData())) {
                    BigDecimal pnl = orderDO.getAvePrice().subtract(sessionDO.getHoldAvePrice()).multiply(sessionDO.getHoldQty());
                    update.setClosePnl("LONG".equals(position) ? pnl : pnl.negate());
                    BigDecimal feeRate = accountDO.getTakerFeeRate() != null ? accountDO.getTakerFeeRate() : BigDecimal.ZERO;
                    update.setOpenFee(feeRate.multiply(sessionDO.getHoldQty().multiply(sessionDO.getHoldAvePrice())));
                    update.setCloseFee(feeRate.multiply(sessionDO.getHoldQty().multiply(sessionDO.getHoldAvePrice())));
                }
                // 记录止盈成功日志
                executionLogService.logSessionWithPrice(sessionDO, ExecutionLogTypeEnum.TAKE_PROFIT_SUCCESS,
                        "止盈成功", String.format("会话[%d]止盈成功，成交价格=%s，盈亏=%s",
                                id, orderDO.getAvePrice(), update.getClosePnl()),
                        orderDO.getAvePrice(), sessionDO.getTakeProfitPrice(), "SUCCESS");
            } else {
                update.setRemark("没有正常的进行止盈，确认是否手动操作订单!!!");
                // 记录止盈失败日志
                executionLogService.logSession(sessionDO, ExecutionLogTypeEnum.TAKE_PROFIT_FAILED,
                        "止盈异常", "没有正常的进行止盈，确认是否手动操作订单", "FAILED");
            }
            log.warn("takeProfit -> 止盈完成, sessionId={}, symbol={}, accountId={}", sessionDO.getId(), accountDO.getSymbol(), accountDO.getId());
            appAccountSessionRepository.updateById(update);
        });
    }

    protected void stopLoss(Long id, D signalBO, String position) {
        SessionBizStatusEnum statusEnum = signalBO.getStatus();
        LocalLock.executeWithoutResult("SESSION_" + id, () -> {
            log.warn("stopLoss -> 尝试止损， sessionId={}", id);
            AppAccountSessionDO sessionDO = appAccountSessionRepository.getById(id);
            if (null == sessionDO) {
                log.warn("stopLoss -> 无效的sessionId， sessionDO={}", id);
                return;
            }
            if (SessionStatusEnum.completed().contains(sessionDO.getStatus())) {
                log.warn("stopLoss -> 当前会话状态已经完结，无需再次操作， sessionId={}, status={}", id, sessionDO.getStatus());
                return;
            }
            if (!statusEnum.equalsByCode(sessionDO.getBizStatus())) {
                log.warn("stopLoss -> 会话状态发生变化，无法继续执行， sessionId={}, expectStatus={}, sessionStatus={}",
                        id, statusEnum, SessionBizStatusEnum.byCode(sessionDO.getBizStatus()));
                return;
            }
            AppAccountDO accountDO = appAccountRepository.getById(sessionDO.getAccountId());

            // 记录止损开始日志
            BigDecimal curPrice = klineCacheFactory.getCurPrice(signalBO.getSymbol(), KlineIntervalEnum.MINUTE_1);
            executionLogService.logSessionWithPrice(sessionDO, ExecutionLogTypeEnum.STOP_LOSS_START,
                    "开始止损", String.format("会话[%d]开始执行止损操作，当前价格=%s，止损价格=%s",
                            id, curPrice, sessionDO.getStopLossPrice()),
                    curPrice, sessionDO.getStopLossPrice(), "SUCCESS");

            AppAccountSessionDO update = new AppAccountSessionDO();
            update.setId(sessionDO.getId());
            update.setBizStatus(SessionBizStatusEnum.STOP_LOSS.code());
            update.setStatus(SessionStatusEnum.STOP_LOSS.code());
            update.setNextCheckTime(LocalDateTime.now().plusSeconds(5));

            AppAccountOrderDO orderDO = appAccountOrderService.closeMarket(sessionDO.getStopLossOrderId(), accountDO,
                    sessionDO, position, curPrice, null);
            if (null != orderDO) {
                update.setStopLossClientOrderId(orderDO.getClientOrderId());
                update.setStopLossOrderId(orderDO.getOrderId());
                if (null != orderDO.getMockData() && MockDataEnum.MOCK.equalsByCode(orderDO.getMockData())) {
                    BigDecimal pnl = orderDO.getAvePrice().subtract(sessionDO.getHoldAvePrice()).multiply(sessionDO.getHoldQty());
                    update.setClosePnl("LONG".equals(position) ? pnl : pnl.negate());
                    BigDecimal feeRate = accountDO.getTakerFeeRate() != null ? accountDO.getTakerFeeRate() : BigDecimal.ZERO;
                    update.setOpenFee(feeRate.multiply(sessionDO.getHoldQty().multiply(sessionDO.getHoldAvePrice())));
                    update.setCloseFee(feeRate.multiply(sessionDO.getHoldQty().multiply(sessionDO.getHoldAvePrice())));
                }
                // 记录止损成功日志
                executionLogService.logSessionWithPrice(sessionDO, ExecutionLogTypeEnum.STOP_LOSS_SUCCESS,
                        "止损成功", String.format("会话[%d]止损成功，成交价格=%s，盈亏=%s",
                                id, orderDO.getAvePrice(), update.getClosePnl()),
                        orderDO.getAvePrice(), sessionDO.getStopLossPrice(), "SUCCESS");
            } else {
                update.setRemark("没有正常的进行止损，确认是否手动操作订单!!!");
                // 记录止损失败日志
                executionLogService.logSession(sessionDO, ExecutionLogTypeEnum.STOP_LOSS_FAILED,
                        "止损异常", "没有正常的进行止损，确认是否手动操作订单", "FAILED");
            }
            log.warn("stopLoss -> 止损完成, sessionId={}, symbol={}, accountId={}", sessionDO.getId(), accountDO.getSymbol(), accountDO.getId());
            appAccountSessionRepository.updateById(update);
        });
    }

    protected void saveSession(AppAccountDO accountDO, AppStrategyInstanceDO instanceDO, Long sessionId,
                               BigDecimal holdQty, BigDecimal holdAvePrice,
                               BigDecimal takeProfitPrice, BigDecimal stopLossPrice,
                               StrategyBizParam bizParam) {
        AppAccountSessionDO sessionDO = new AppAccountSessionDO();
        sessionDO.setId(sessionId);
        sessionDO.setAccountId(accountDO.getId());
        sessionDO.setUserId(accountDO.getUserId());
        sessionDO.setExchange(accountDO.getExchange());
        sessionDO.setSymbol(instanceDO.getSymbol());
        sessionDO.setStrategyCode(instanceDO.getCode());
        sessionDO.setBaseParam(instanceDO.getBaseParam());
        sessionDO.setRunParam(instanceDO.getRunParam());
        sessionDO.setHoldQty(holdQty);
        sessionDO.setSyncStatus(SyncStatusEnum.NO_SYNC.getCode());
        sessionDO.setStatus(SessionStatusEnum.RUNNING.code());
        sessionDO.setBizStatus(SessionBizStatusEnum.WAIT_PROFIT.code());
        sessionDO.setNextCheckTime(LocalDateTime.now().plusSeconds(5));
        sessionDO.setStrategyInstanceId(instanceDO.getId());
        sessionDO.setOpenFee(BigDecimal.ZERO);
        sessionDO.setCloseFee(BigDecimal.ZERO);
        sessionDO.setClosePnl(BigDecimal.ZERO);
        sessionDO.setHoldAvePrice(holdAvePrice);
        sessionDO.setMockData(TradeTypeEnum.MOCK.equalsByCode(accountDO.getTradeType()) ? MockDataEnum.MOCK.code() : MockDataEnum.REAL.code());
        sessionDO.setTakeProfitPrice(takeProfitPrice);
        sessionDO.setStopLossPrice(stopLossPrice);
        sessionDO.setBizParam(JSON.toJSONString(bizParam));
        //保存会话数据
        appAccountSessionRepository.save(sessionDO);

        // 记录会话创建日志
        executionLogService.logSessionWithPrice(sessionDO, ExecutionLogTypeEnum.SESSION_CREATED,
                "会话创建", String.format("新会话创建成功，持仓数量=%s，均价=%s，止盈价=%s，止损价=%s",
                        holdQty, holdAvePrice, takeProfitPrice, stopLossPrice),
                holdAvePrice, takeProfitPrice, "SUCCESS");
    }

}

