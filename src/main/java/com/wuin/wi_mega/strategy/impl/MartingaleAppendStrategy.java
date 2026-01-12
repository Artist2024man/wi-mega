package com.wuin.wi_mega.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.wuin.wi_mega.binance.bo.Kline;
import com.wuin.wi_mega.common.enums.*;
import com.wuin.wi_mega.common.util.DateUtils;
import com.wuin.wi_mega.common.util.LocalLock;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import com.wuin.wi_mega.mega_market.MegaMarketClient;
import com.wuin.wi_mega.mega_market.model.Signal;
import com.wuin.wi_mega.mega_market.model.SignalResult;
import com.wuin.wi_mega.mega_market.model.StrategySignalRequest;
import com.wuin.wi_mega.model.bo.signal.SessionMartingaleRunningBO;
import com.wuin.wi_mega.model.bo.StrategyBizOrderParam;
import com.wuin.wi_mega.model.bo.signal.StrategyMartingaleStartSignalBO;
import com.wuin.wi_mega.model.bo.base.StrategyMartingaleBaseParam;
import com.wuin.wi_mega.model.bo.biz.StrategyMartingaleBizParam;
import com.wuin.wi_mega.model.bo.running.StrategyMartingaleRunParam;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppAccountOrderDO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.repository.domain.AppStrategyInstanceDO;
import com.wuin.wi_mega.strategy.TradeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
@Slf4j
public class MartingaleAppendStrategy extends TradeStrategy<StrategyMartingaleStartSignalBO, SessionMartingaleRunningBO> {

    @Override
    protected StrategyMartingaleStartSignalBO executeInner(AppAccountDO accountDO, AppStrategyInstanceDO instanceDO, SymbolEnum symbol) {
        StrategyMartingaleBaseParam baseParam = JSON.parseObject(instanceDO.getBaseParam(), StrategyMartingaleBaseParam.class);
        Kline cur = klineCacheFactory.get(symbol, KlineIntervalEnum.MINUTE_1, DateUtils.getMinuteStartMillis(LocalDateTime.now()));

        // 增加空值检查
        if (cur == null) {
            log.warn("executeInner -> 无法获取到当前时间的K线, accountId={}, symbol={}", accountDO.getId(), symbol);
            return null;
        }
        long startTime = System.currentTimeMillis();
        SignalResult result = this.requestSignal(symbol, baseParam, cur.getStart());
        log.warn("executeInner -> accountId={} result={}, cost={}", accountDO.getId(), JSON.toJSONString(result), System.currentTimeMillis() - startTime);
        return Signal.NONE.equals(result.getSignal()) ? null : new StrategyMartingaleStartSignalBO(accountDO.getId(), symbol, cur.getStart(), result);
    }


    @Override
    public void handlerStartSignal(StrategyMartingaleStartSignalBO signalBO) {
        if (!Signal.isOpenSignal(signalBO.getResult().getSignal())) {
            log.warn("handlerStartSignal -> 当前信号不为开仓信号, signalBO={}", JSON.toJSONString(signalBO));
            return;
        }
        Kline cur = klineCacheFactory.get(signalBO.getSymbol(), KlineIntervalEnum.MINUTE_1, DateUtils.getMinuteStartMillis(LocalDateTime.now()));
        if (null == cur) {
            log.warn("handlerStartSignal -> 无法获取到当前时间的K线,signalBO={}", JSON.toJSONString(signalBO));
            return;
        }
        if (cur.getStart().compareTo(signalBO.getStart()) > 0) {
            log.warn("handlerStartSignal -> 信号已经失效，,signalBO={}, cur.start={}", JSON.toJSONString(signalBO), cur.getStart());
            strategySignalCache.remove(signalBO);
            return;
        }
        BigDecimal curPrice = cur.getClose();
        String position = Signal.LONG.equals(signalBO.getResult().getSignal()) ? "LONG" : "SHORT";
        if ("LONG".equals(position)) {
            // 多单：当前价格 >= 信号价格时开仓
            if (BigDecimal.valueOf(signalBO.getResult().getPrice()).compareTo(curPrice) >= 0) {
                this.tryStartNewSession(signalBO);
            }
        } else {
            // 空单：当前价格 <= 信号价格时开仓
            if (BigDecimal.valueOf(signalBO.getResult().getPrice()).compareTo(curPrice) <= 0) {
                this.tryStartNewSession(signalBO);
            }
        }
    }

    @Override
    protected void handlerRunningSignal(SessionMartingaleRunningBO signalBO) {
        SessionBizStatusEnum statusEnum = signalBO.getStatus();
        if (SessionBizStatusEnum.HOLD_ALL.equals(statusEnum)) {
            return;
        }
        BigDecimal curPrice = klineCacheFactory.getCurPrice(signalBO.getSymbol(), KlineIntervalEnum.MINUTE_1);
        long start = DateUtils.getMinuteStartMillis(LocalDateTime.now());
        if (SessionBizStatusEnum.WAIT_PROFIT.equals(statusEnum)) {
            StrategyBizOrderParam append = signalBO.touchAppend(curPrice);
            StrategyBizOrderParam reverse = null;
            if (append == null) {
                reverse = signalBO.touchReverse(curPrice);
            }
            if (signalBO.needTakeProfit(curPrice)) {
                log.warn("handlerRunningSignal -> WAIT_PROFIT needTakeProfit curPrice={}, id={}", curPrice, signalBO.getId());
                this.doTakeProfit(signalBO, start);
            } else if (signalBO.needStopLoss(curPrice)) {
                log.warn("handlerRunningSignal -> WAIT_PROFIT needStopLoss curPrice={}, id={}", curPrice, signalBO.getId());
                this.stopLoss(signalBO.getId(), signalBO, signalBO.getOpenPosition());
            } else if (null != append) {
                log.warn("handlerRunningSignal -> WAIT_PROFIT needAppend id={}, curPrice={}, append={}", signalBO.getId(), curPrice, JSON.toJSONString(append));
                this.append(signalBO, append.getIdx());
            } else if (null != reverse) {
                log.warn("handlerRunningSignal -> WAIT_PROFIT needReverse id={}, curPrice={}, append={}", signalBO.getId(), curPrice, JSON.toJSONString(reverse));
                this.reverse(signalBO.getId(), statusEnum);
            }
        }
    }

    @Override
    protected SessionMartingaleRunningBO buildRunningSingle(AppAccountSessionDO sessionDO) {
        return new SessionMartingaleRunningBO(sessionDO);
    }

    @Override
    public StrategyEnum strategy() {
        return StrategyEnum.MARTINGALE_APPEND;
    }

    private void tryStartNewSession(StrategyMartingaleStartSignalBO signalBO) {
        Long sessionId = SimpleSnowflake.nextId();
        LocalLock.executeWithoutResult("SESSION_" + sessionId, () -> {
            if (CollectionUtils.isNotEmpty(appAccountSessionRepository.listByAccountIdAndStatusList(signalBO.getAccountId(), SessionStatusEnum.running()))) {
                log.warn("tryNewSession -> 当前账号已经存在运行的会话，忽略本次开仓，accountId={}", signalBO.getAccountId());
                return;
            }
            AppAccountDO accountDO = appAccountRepository.getById(signalBO.getAccountId());
            if (StrategyStatusEnum.STOP.equalsByCode(accountDO.getStrategyStatus())) {
                log.warn("tryNewSession -> 已经停止策略，忽略本次开仓，accountId={}", signalBO.getAccountId());
                return;
            }
            AppStrategyInstanceDO instanceDO = appStrategyInstanceRepository.getById(accountDO.getStrategyInstanceId());
            if (null == instanceDO) {
                log.warn("tryNewSession -> instance is null, ignore, accountId={}", signalBO.getAccountId());
                return;
            }
            StrategyMartingaleRunParam runParam = JSON.parseObject(instanceDO.getRunParam(), StrategyMartingaleRunParam.class);

            BigDecimal curPrice = klineCacheFactory.getCurPrice(signalBO.getSymbol(), KlineIntervalEnum.MINUTE_1);

            String position = signalBO.getResult().getSignal().name();
            //开仓
            AppAccountOrderDO orderDO = appAccountOrderService.openMarket(null, accountDO, sessionId, position, curPrice, runParam.getOpqty());
            BigDecimal avePrice = null == orderDO ? curPrice : orderDO.getAvePrice();

            BigDecimal takeProfitPrice = null;
            BigDecimal stopLossPrice = null;
            if (null != runParam.getTp() && runParam.getTp().compareTo(BigDecimal.ZERO) > 0) {
                takeProfitPrice = "LONG".equals(position) ? avePrice.add(runParam.getTp()) : avePrice.subtract(runParam.getTp());
            }
            if (null != runParam.getSl() && runParam.getSl().compareTo(BigDecimal.ZERO) > 0) {
                stopLossPrice = "LONG".equals(position) ? avePrice.subtract(runParam.getSl()) : avePrice.add(runParam.getSl());
            }
            // ---------------- 业务参数 ---------------- //
            StrategyMartingaleBizParam bizParam = new StrategyMartingaleBizParam();
            // ================= 写入开仓订单数据 =================
            bizParam.getOpen().setPosition(position);
            bizParam.getOpen().setPrice(curPrice);
            bizParam.getOpen().setQty(runParam.getOpqty());
            bizParam.getOpen().setOrderId(null == orderDO ? SimpleSnowflake.nextId() : orderDO.getId());
            bizParam.getOpen().setAvgPrice(null == orderDO ? curPrice : orderDO.getAvePrice());
            // ================= 初始化补仓数据 =================
            if (CollectionUtils.isNotEmpty(runParam.getAppends())) {
                for (int i = 0; i < runParam.getAppends().size(); i++) {
                    StrategyMartingaleRunParam.QtyPriceBO appBO = runParam.getAppends().get(i);
                    StrategyBizOrderParam append = new StrategyBizOrderParam(i, position, appBO.getQty());
                    if (i == 0) { //首次开仓需要填充第一次补仓数据
                        append.setPrice("LONG".equals(position) ? avePrice.subtract(appBO.getLos()) : avePrice.add(appBO.getLos()));
                    }
                    bizParam.getAppends().add(append);
                }
            }
            // ================= 初始化对冲数据 =================
            if (null != runParam.getRslos()) {
                bizParam.setReverse(new StrategyBizOrderParam());
                bizParam.getReverse().setPosition(position.equals("LONG") ? "SHORT" : "LONG");
                bizParam.getReverse().setQty(runParam.getOpqty());
            }
            //保存会话数据
            super.saveSession(accountDO, instanceDO, sessionId, runParam.getOpqty(), avePrice, takeProfitPrice, stopLossPrice, bizParam);
        });
    }

    private void append(SessionMartingaleRunningBO signalBO, Integer idx) {
        SessionBizStatusEnum statusEnum = signalBO.getStatus();
        Long id = signalBO.getId();

        LocalLock.executeWithoutResult("SESSION_" + id, () -> {
            log.warn("MartingaleAppendStrategy.append -> 尝试开始加仓操作， sessionDO={}", id);
            AppAccountSessionDO sessionDO = appAccountSessionRepository.getById(id);
            if (null == sessionDO) {
                log.warn("MartingaleAppendStrategy.append -> 无效的sessionId， sessionDO={}", id);
                return;
            }

            if (SessionStatusEnum.completed().contains(sessionDO.getStatus())) {
                log.warn("MartingaleAppendStrategy.append -> 当前会话状态已经完结，无需再次操作， sessionId={}, status={}", id, sessionDO.getStatus());
                return;
            }
            StrategyMartingaleBizParam bizParam = JSON.parseObject(sessionDO.getBizParam(), StrategyMartingaleBizParam.class);
            StrategyMartingaleRunParam runParam = JSON.parseObject(sessionDO.getRunParam(), StrategyMartingaleRunParam.class);
            if (!statusEnum.equalsByCode(sessionDO.getBizStatus())) {
                log.warn("MartingaleAppendStrategy.append -> 会话状态发生变化，无法继续执行， sessionId={}, expectStatus={}, sessionStatus={}",
                        id, statusEnum, SessionBizStatusEnum.byCode(sessionDO.getBizStatus()));
                return;
            }
            AppAccountDO accountDO = appAccountRepository.getById(sessionDO.getAccountId());

            StrategyBizOrderParam append = bizParam.getAppend(idx);

            if (null == append) {
                log.warn("MartingaleAppendStrategy.append -> 没有对应补仓数据，sessionId={}, idx={}", sessionDO.getId(), idx);
                return;
            }
            if (append.isDone()) {
                log.warn("MartingaleAppendStrategy.append -> 当前补仓数据已经完成补仓，无法再次执行，sessionId={}, idx={}", sessionDO.getId(), idx);
                return;
            }

            AppAccountOrderDO orderDO = appAccountOrderService.openMarket(append.getOrderId(), accountDO, sessionDO.getId(),
                    append.getPosition(), append.getPrice(), append.getQty());

            AppAccountSessionDO update = new AppAccountSessionDO();
            update.setId(sessionDO.getId());
//            update.setBizStatus(SessionBizStatusEnum.WAIT_APPEND_PROFIT.code());
            //补仓信息
            append.setOrderId(orderDO.getId());
            append.setAvgPrice(orderDO.getAvePrice());
            // 更新持仓均价、持仓数量
            BigDecimal openAmount = sessionDO.getHoldAvePrice().multiply(sessionDO.getHoldQty());
            BigDecimal appendAmount = orderDO.getAvePrice().multiply(orderDO.getQty());
            BigDecimal avePrice = openAmount.add(appendAmount).divide(sessionDO.getHoldQty().add(orderDO.getQty()), 6, RoundingMode.HALF_UP);
            update.setHoldAvePrice(avePrice);
            update.setHoldQty(sessionDO.getHoldQty().add(append.getQty()));
            //尝试更新下一次补仓信息
            int nextIdx = idx + 1;
            StrategyBizOrderParam nextAppend = bizParam.getAppend(nextIdx);
            if (null != nextAppend) {
                StrategyMartingaleRunParam.QtyPriceBO pBO = runParam.getAppends().get(nextIdx);
                if (null == pBO) {
                    log.warn("MartingaleAppendStrategy.append -> 存在补仓业务数据但是没有获取到对应运行配置，sessionId={}, nextIdx={}", id, nextIdx);
                    return;
                }
                nextAppend.setPrice("LONG".equals(nextAppend.getPosition()) ? avePrice.subtract(pBO.getLos()) : avePrice.add(pBO.getLos()));
                nextAppend.setQty(pBO.getQty());
            }
            //尝试更新对冲信息
            StrategyBizOrderParam reverse = bizParam.getReverse();
            if (null != reverse) {
                reverse.setPrice("LONG".equals(append.getPosition()) ? avePrice.subtract(runParam.getRslos()) : avePrice.add(runParam.getRslos()));
                reverse.setQty(update.getHoldQty());
            }
            //更新止盈止损值
            BigDecimal takeProfit = null;
            BigDecimal stopLoss = null;
            if (null != runParam.getTp() && runParam.getTp().compareTo(BigDecimal.ZERO) > 0) {
                takeProfit = "LONG".equals(bizParam.getOpen().getPosition()) ? avePrice.add(runParam.getTp()) : avePrice.subtract(runParam.getTp());
            }
            if (null != runParam.getSl() && runParam.getSl().compareTo(BigDecimal.ZERO) > 0) {
                stopLoss = "LONG".equals(bizParam.getOpen().getPosition()) ? avePrice.subtract(runParam.getSl()) : avePrice.add(runParam.getSl());
            }
            update.setRemark("达到加仓条件，完成第一次加仓!");
            update.setTakeProfitPrice(takeProfit);
            update.setStopLossPrice(stopLoss);
            update.setBizParam(JSON.toJSONString(bizParam));
            appAccountSessionRepository.updateById(update);
            log.info("MartingaleAppendStrategy.append -> 订单状态流转，sessionId={}, oldStatus={}, newStatus={}, remark={}", sessionDO.getId(), statusEnum.code(), update.getBizStatus(), update.getRemark());
        });
    }

    public void reverse(Long id, SessionBizStatusEnum statusEnum) {
        LocalLock.executeWithoutResult("SESSION_" + id, () -> {
            log.warn("MartingaleAppendStrategy.reverse -> 尝试开始对冲操作， sessionDO={}", id);
            AppAccountSessionDO sessionDO = appAccountSessionRepository.getById(id);
            if (null == sessionDO) {
                log.warn("MartingaleAppendStrategy.reverse -> 无效的sessionId， sessionDO={}", id);
                return;
            }
            if (SessionStatusEnum.completed().contains(sessionDO.getStatus())) {
                log.warn("MartingaleAppendStrategy.reverse -> 当前会话状态已经完结，无需再次操作， sessionId={}, status={}", id, sessionDO.getStatus());
                return;
            }
            StrategyMartingaleBizParam bizParam = JSON.parseObject(sessionDO.getBizParam(), StrategyMartingaleBizParam.class);
            if (!statusEnum.equalsByCode(sessionDO.getBizStatus())) {
                log.warn("MartingaleAppendStrategy.reverse -> 会话状态发生变化，无法继续执行， sessionId={}, expectStatus={}, sessionStatus={}",
                        id, statusEnum, SessionBizStatusEnum.byCode(sessionDO.getBizStatus()));
                return;
            }
            AppAccountDO accountDO = appAccountRepository.getById(sessionDO.getAccountId());
            StrategyBizOrderParam reverse = bizParam.getReverse();
            if (null == reverse) {
                log.warn("MartingaleAppendStrategy.reverse -> 没有找到任何对冲配置， sessionId={}", id);
                return;
            }

            if (null == reverse.getPrice() || reverse.getQty() == null || reverse.isDone()) {
                log.warn("MartingaleAppendStrategy.reverse -> 对冲配置价格或数量为空，无法对冲， sessionId={}, reverse={}", id, JSON.toJSONString(reverse));
                return;
            }

            AppAccountOrderDO orderDO = appAccountOrderService.openMarket(reverse.getOrderId(), accountDO, sessionDO.getId(),
                    reverse.getPosition(), reverse.getPrice(), sessionDO.getHoldQty());
            //停止策略
            AppAccountDO updateAcc = new AppAccountDO();
            updateAcc.setId(accountDO.getId());
            updateAcc.setStrategyStatus(StrategyStatusEnum.STOP.code());
            updateAcc.setRemark("触发对冲，停止策略");
            appAccountRepository.updateById(updateAcc);
            //停止会话
            AppAccountSessionDO update = new AppAccountSessionDO();
            update.setId(sessionDO.getId());
            update.setBizStatus(SessionBizStatusEnum.HOLD_ALL.code());
            reverse.setAvgPrice(orderDO.getAvePrice());
            reverse.setOrderId(orderDO.getId());
            update.setBizParam(JSON.toJSONString(bizParam));
            appAccountSessionRepository.updateById(update);
            log.info("MartingaleAppendStrategy.reverse -> 订单状态流转，sessionId={}, oldStatus={}, newStatus={}, remark={}", sessionDO.getId(), sessionDO.getStatus(), update.getStatus(), update.getRemark());
        });
    }

    protected SignalResult requestSignal(SymbolEnum symbol, StrategyMartingaleBaseParam baseParam, Long start) {
        return requestSignal(-1L, symbol, baseParam, null, false, false, start);
    }

    protected SignalResult requestSignal(Long sessionId, SymbolEnum symbol, StrategyMartingaleBaseParam baseParam, String positionSide, Boolean closeLong, Boolean closeShort, Long start) {
        StrategySignalRequest request = new StrategySignalRequest();
        //基础参数
        request.setSymbol(symbol.name());
        request.setInterval(baseParam.getInterval());
        request.setPositionSide(positionSide);
        //指标数据
        request.setDualMode(true);
        request.setRsiPeriod(baseParam.getRsiPeriod());
        request.setRsiOversold(baseParam.getRsiOversold());
        request.setRsiOverbought(baseParam.getRsiOverbought());
        request.setEmaFast(baseParam.getEmaFast());
        request.setEmaSlow(baseParam.getEmaSlow());
        request.setVolumePeriod(baseParam.getVolumePeriod());
        request.setVolumeThreshold(baseParam.getVolumeThreshold());
        request.setMinScore(baseParam.getMinScore());
        request.setAllowCloseLong(closeLong);
        request.setAllowCloseShort(closeShort);
        return MegaMarketClient.requestSignal(sessionId, request, start);
    }

    protected void doTakeProfit(SessionMartingaleRunningBO signalBO, Long start) {
        AppAccountSessionDO sessionDO = appAccountSessionRepository.getById(signalBO.getId());
        StrategyMartingaleBaseParam baseParam = JSON.parseObject(sessionDO.getBaseParam(), StrategyMartingaleBaseParam.class);
        log.warn("doTakeProfit -> signalBO={}", JSON.toJSONString(signalBO));
        boolean closeLong = "LONG".equals(signalBO.getOpenPosition());
        boolean closeShort = "SHORT".equals(signalBO.getOpenPosition());
        SignalResult result = this.requestSignal(sessionDO.getId(), signalBO.getSymbol(), baseParam, signalBO.getOpenPosition(), closeLong, closeShort, start);
        if ("LONG".equals(signalBO.getOpenPosition())) {
            if (Signal.CLOSE_LONG.equals(result.getSignal())) {
                super.takeProfit(signalBO.getId(), signalBO, signalBO.getOpenPosition());
            }
        } else {
            if (Signal.CLOSE_SHORT.equals(result.getSignal())) {
                super.takeProfit(signalBO.getId(), signalBO, signalBO.getOpenPosition());
            }
        }
    }
}
