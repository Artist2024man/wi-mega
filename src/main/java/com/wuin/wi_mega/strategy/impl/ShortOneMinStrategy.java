package com.wuin.wi_mega.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.wuin.wi_mega.binance.bo.Kline;
import com.wuin.wi_mega.common.enums.*;
import com.wuin.wi_mega.common.util.DateUtils;
import com.wuin.wi_mega.common.util.LocalLock;
import com.wuin.wi_mega.common.util.SimpleSnowflake;
import com.wuin.wi_mega.model.bo.StrategyBizOrderParam;
import com.wuin.wi_mega.model.bo.biz.StrategyMartingaleBizParam;
import com.wuin.wi_mega.model.bo.running.StrategyMartingaleRunParam;
import com.wuin.wi_mega.model.bo.signal.SessionOneMinRunningBO;
import com.wuin.wi_mega.model.bo.signal.StrategyOneMinStartSignalBO;
import com.wuin.wi_mega.model.bo.base.StrategyOneMinBaseParam;
import com.wuin.wi_mega.model.bo.running.StrategyOneMinRunParam;
import com.wuin.wi_mega.model.bo.biz.StrategyOneMinBizParam;
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
public class ShortOneMinStrategy extends TradeStrategy<StrategyOneMinStartSignalBO, SessionOneMinRunningBO> {

    @Override
    public StrategyEnum strategy() {
        return StrategyEnum.ONE_MIN_SHORT;
    }

    @Override
    public void handlerStartSignal(StrategyOneMinStartSignalBO signalBO) {
        Kline cur = klineCacheFactory.get(signalBO.getSymbol(), KlineIntervalEnum.MINUTE_1, DateUtils.getMinuteStartMillis(LocalDateTime.now()));
        if (null == cur) {
            log.warn("handlerAccountSignal -> 无法获取到当前时间的K线,signalBO={}", JSON.toJSONString(signalBO));
            return;
        }

        if (cur.getStart().compareTo(signalBO.getStart()) > 0) {
            log.warn("handlerAccountSignal -> 信号已经失效，,signalBO={}, cur.start={}", JSON.toJSONString(signalBO), cur.getStart());
            strategySignalCache.remove(signalBO);
            return;
        }
        BigDecimal curPrice = cur.getClose();
        if ("LONG".equals(signalBO.getPosition())) {
            if (signalBO.getOpenPrice().compareTo(curPrice) <= 0) {
                this.tryNewSession(signalBO);
            }
        } else {
            if (signalBO.getOpenPrice().compareTo(curPrice) >= 0) {
                this.tryNewSession(signalBO);
            }
        }
    }

    @Override
    public void handlerRunningSignal(SessionOneMinRunningBO signalBO) {
        SessionBizStatusEnum statusEnum = signalBO.getStatus();
        if (SessionBizStatusEnum.HOLD_ALL.equals(statusEnum)) {
            return;
        }
        BigDecimal curPrice = klineCacheFactory.getCurPrice(signalBO.getSymbol(), KlineIntervalEnum.MINUTE_1);
        if (SessionBizStatusEnum.WAIT_PROFIT.equals(statusEnum)) {
            StrategyBizOrderParam append = signalBO.touchAppend(curPrice);
            StrategyBizOrderParam reverse = null;
            if (append == null) {
                reverse = signalBO.touchReverse(curPrice);
            }
            if (signalBO.needTakeProfit(curPrice)) {
                log.warn("handlerRunningSignal -> WAIT_PROFIT needTakeProfit curPrice={}, id={}", curPrice, signalBO.getId());
                this.takeProfit(signalBO.getId(), signalBO, signalBO.getOpenPosition());
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
    protected SessionOneMinRunningBO buildRunningSingle(AppAccountSessionDO sessionDO) {
        return new SessionOneMinRunningBO(sessionDO);
    }

    @Override
    protected StrategyOneMinStartSignalBO executeInner(AppAccountDO accountDO, AppStrategyInstanceDO instanceDO, SymbolEnum symbol) {
        StrategyOneMinBaseParam baseParam = JSON.parseObject(instanceDO.getBaseParam(), StrategyOneMinBaseParam.class);
        StrategyOneMinRunParam runParam = JSON.parseObject(instanceDO.getRunParam(), StrategyOneMinRunParam.class);
        return executeInner(accountDO, baseParam, runParam, symbol);
    }

    protected StrategyOneMinStartSignalBO executeInner(AppAccountDO accountDO, StrategyOneMinBaseParam baseParam, StrategyOneMinRunParam runParam, SymbolEnum symbol) {
        log.info("BaEthOneMinStrategy -> start, accountId={}, symbol={}, strategyName={}", accountDO.getId(), symbol, strategy().getMessage());
        LocalDateTime now = LocalDateTime.now();
        Kline cur = klineCacheFactory.get(symbol, KlineIntervalEnum.MINUTE_1, DateUtils.getMinuteStartMillis(now));
        if (null == cur) {
            log.warn("BaEthOneMinStrategy -> has no current kline");
            return null;
        }
        if (cur.maxDistance().compareTo(baseParam.getMdis()) < 0) {
            log.warn("BaEthOneMinStrategy -> max distance less than: {}, maxDistance={}", baseParam.getMdis(), cur.maxDistance());
            return null;
        }

        int score = 50;

        Kline preOneMin = klineCacheFactory.get(symbol, KlineIntervalEnum.MINUTE_1, DateUtils.getMinuteStartMillis(now.minusMinutes(1)));

        BigDecimal last5Diff = klineCacheFactory.getMaxSignedDiff(symbol, KlineIntervalEnum.MINUTE_1, cur, now, 5);

        Kline cur5m = klineCacheFactory.get(symbol, KlineIntervalEnum.MINUTE_5, DateUtils.getPeriodStartMillis(now, KlineIntervalEnum.MINUTE_5.getInterval()));
        BigDecimal last5Diff5m = klineCacheFactory.getMaxSignedDiff(symbol, KlineIntervalEnum.MINUTE_5, cur5m, now, 5);

        String position = calSide(cur, preOneMin, last5Diff, last5Diff5m, baseParam);

        if (null == position) {
            return null;
        }

        String last5Side = last5Diff.compareTo(BigDecimal.ZERO) < 0 ? "LONG" : "SHORT";
        int change5Sore = calLast5Score(symbol, last5Diff.abs());
        if (position.equals(last5Side)) { //同向压力，降低信心值
            score -= change5Sore;
        } else { //反向，增加信心
            score += change5Sore;
        }

        BigDecimal last10Diff = klineCacheFactory.getMaxSignedDiff(symbol, KlineIntervalEnum.MINUTE_1, cur, now, 10);

        String last10Side = last10Diff.compareTo(BigDecimal.ZERO) < 0 ? "LONG" : "SHORT";

        int change10Score = calLast10Score(symbol, last10Diff.abs());

        if (position.equals(last10Side)) { //同向压力，降低信心值
            score -= change10Score;
        } else { //反向，增加信心
            score += change10Score;
        }
        BigDecimal price = "LONG".equals(position) ? cur.getLow().add(cur.maxDistance().multiply(runParam.getLbuf()))
                : cur.getHigh().subtract(cur.maxDistance().multiply(runParam.getSbuf()));

        return new StrategyOneMinStartSignalBO(accountDO.getId(), symbol, score, price, position, cur.getStart());
    }


    /**
     * 最近十分钟内的价差
     */
    private BigDecimal last10Diff(SymbolEnum symbol, LocalDateTime now, Kline cur) {
        Kline preTenMin = klineCacheFactory.get(symbol, KlineIntervalEnum.MINUTE_1, DateUtils.getMinuteStartMillis(now.minusMinutes(11)));
        if (null == preTenMin) {
            return BigDecimal.ZERO;
        }
        return preTenMin.getOpen().subtract(cur.getClose());
    }

//    private BigDecimal last5Diff(SymbolEnum symbol, LocalDateTime now, Kline cur) {
//        Kline preFourMin = klineCacheFactory.get(symbol, DateUtils.getMinuteStartMillis(now.minusMinutes(4)));
//        if (null == preFourMin) {
//            return BigDecimal.ZERO;
//        }
//        return preFourMin.getOpen().subtract(cur.getClose());
//    }

    /**
     * 1.如果上一分钟开盘==收盘，则忽略这次
     * 2.如果上一分钟 开盘 - 收盘 绝对值大于某个阈值，则 反向操作
     * 3.当前这一分钟 和 上一分钟 对比 最低价决定本次趋势
     *
     * @param cur    当前K线
     * @param preOne 前一分钟K线
     * @return 方向：LONG / SHORT
     */
    private String calSide(Kline cur, Kline preOne, BigDecimal last5Diff, BigDecimal last5Diff5m, StrategyOneMinBaseParam baseParam) {
        if (preOne == null || preOne.getOpen().compareTo(preOne.getClose()) == 0) {
            return null;
        }

        BigDecimal absP5 = last5Diff5m.abs();
        if (absP5.compareTo(baseParam.getRdis55m()) >= 0) {
            String last55mPosition = last5Diff5m.compareTo(BigDecimal.ZERO) > 0 ? "SHORT" : "LONG";
            log.warn("calSide -> last 5 5m kline diff gather:{}, reverse, absP5={}, last55mPosition={}", baseParam.getRdis55m(), absP5, last55mPosition);
            return last55mPosition.equals("LONG") ? "SHORT" : "LONG";
        }

        String preSide = preOne.getSide();
        log.info("calSide -> preSide={}", preSide);
        BigDecimal absP = preOne.getClose().subtract(preOne.getOpen()).abs();

        if (absP.compareTo(baseParam.getRsdis()) >= 0) {
            log.warn("calSide -> single diff gather:{}, reverse, absP={}, preSide={}", baseParam.getRsdis(), absP, preSide);
            return preSide.equals("LONG") ? "SHORT" : "LONG";
        }

        //最近5跟K线最大涨跌幅超过阈值则直接反转
        if (last5Diff.abs().compareTo(baseParam.getRdis5()) >= 0) {
            log.warn("calSide -> last 5 kline diff is:{}, threshold={}, reverse", last5Diff, baseParam.getRdis5());
            return last5Diff.compareTo(BigDecimal.ZERO) > 0 ? "SHORT" : "LONG";
        }

        BigDecimal lowP = cur.getLow().subtract(preOne.getLow()).abs();

        if (lowP.compareTo(baseParam.getMldif()) < 0) {
            log.info("calSide -> diff is too small, ignore, minLowDiff={}, diff={}", baseParam.getMldif(), lowP);
            return null;
        }

        return cur.getLow().compareTo(preOne.getLow()) > 0 ? "LONG" : "SHORT";
    }

    private int calLast10Score(SymbolEnum symbol, BigDecimal last10DiffAbs) {
        if (symbol.name().startsWith("ETH")) {
            return calLast10ScoreEth(last10DiffAbs);
        } else {
            return calLast10ScoreZec(last10DiffAbs);
        }
    }

    private int calLast5Score(SymbolEnum symbol, BigDecimal last10DiffAbs) {
        if (symbol.name().startsWith("ETH")) {
            return calLast5ScoreEth(last10DiffAbs);
        } else {
            return calLast5ScoreZec(last10DiffAbs);
        }
    }

    private int calLast10ScoreEth(BigDecimal last10DiffAbs) {

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(6)) < 0) {
            return 0;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(10)) < 0) {
            return 3;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(18)) < 0) {
            return 11;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(30)) < 0) {
            return 17;
        }

        return 20;
    }

    private int calLast5ScoreEth(BigDecimal last10DiffAbs) {
        if (last10DiffAbs.compareTo(BigDecimal.valueOf(1)) < 0) {
            return 0;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(3)) < 0) {
            return 5;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(6)) < 0) {
            return 15;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(8)) < 0) {
            return 20;
        }
        return 30;
    }

    private int calLast10ScoreZec(BigDecimal last10DiffAbs) {
        if (last10DiffAbs.compareTo(BigDecimal.valueOf(0.5)) < 0) {
            return 0;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(1.2)) < 0) {
            return 3;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(2)) < 0) {
            return 11;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(3)) < 0) {
            return 17;
        }

        return 20;
    }

    private int calLast5ScoreZec(BigDecimal last10DiffAbs) {
        if (last10DiffAbs.compareTo(BigDecimal.valueOf(0.4)) < 0) {
            return 0;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(0.8)) < 0) {
            return 5;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(1.3)) < 0) {
            return 15;
        }

        if (last10DiffAbs.compareTo(BigDecimal.valueOf(1.8)) < 0) {
            return 20;
        }
        return 30;
    }

    private void tryNewSession(StrategyOneMinStartSignalBO signalBO) {
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
            StrategyOneMinRunParam runParam = JSON.parseObject(instanceDO.getRunParam(), StrategyOneMinRunParam.class);
            BigDecimal prefectPrice = signalBO.getOpenPrice();
            //开仓
            AppAccountOrderDO orderDO = appAccountOrderService.openMarket(null, accountDO, sessionId, signalBO.getPosition(), prefectPrice, runParam.getOpqty());
            BigDecimal avgPrice = null == orderDO ? prefectPrice : orderDO.getAvePrice();

            String position = signalBO.getPosition();

            BigDecimal takeProfitPrice = null;
            BigDecimal stopLossPrice = null;
            if (null != runParam.getTp() && runParam.getTp().compareTo(BigDecimal.ZERO) > 0) {
                takeProfitPrice = "LONG".equals(position) ? avgPrice.add(runParam.getTp()) : avgPrice.subtract(runParam.getTp());
            }
            if (null != runParam.getSl() && runParam.getSl().compareTo(BigDecimal.ZERO) > 0) {
                stopLossPrice = "LONG".equals(position) ? avgPrice.subtract(runParam.getSl()) : avgPrice.add(runParam.getSl());
            }
            // ---------------- 业务参数 ---------------- //
            StrategyMartingaleBizParam bizParam = new StrategyMartingaleBizParam();
            // ================= 写入开仓订单数据 =================
            bizParam.getOpen().setPosition(position);
            bizParam.getOpen().setPrice(prefectPrice);
            bizParam.getOpen().setQty(runParam.getOpqty());
            bizParam.getOpen().setOrderId(null == orderDO ? SimpleSnowflake.nextId() : orderDO.getId());
            bizParam.getOpen().setAvgPrice(null == orderDO ? prefectPrice : orderDO.getAvePrice());
            // ================= 初始化补仓数据 =================
            if (CollectionUtils.isNotEmpty(runParam.getAppends())) {
                for (int i = 0; i < runParam.getAppends().size(); i++) {
                    StrategyMartingaleRunParam.QtyPriceBO appBO = runParam.getAppends().get(i);
                    StrategyBizOrderParam append = new StrategyBizOrderParam(i, position, appBO.getQty());
                    if (i == 0) { //首次开仓需要填充第一次补仓数据
                        append.setPrice("LONG".equals(position) ? avgPrice.subtract(appBO.getLos()) : avgPrice.add(appBO.getLos()));
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
            super.saveSession(accountDO, instanceDO, sessionId, runParam.getOpqty(), avgPrice, takeProfitPrice, stopLossPrice, bizParam);
        });
    }

    private void append(SessionOneMinRunningBO signalBO, Integer idx) {
        SessionBizStatusEnum statusEnum = signalBO.getStatus();
        Long id = signalBO.getId();

        LocalLock.executeWithoutResult("SESSION_" + id, () -> {
            log.warn("append -> 尝试开始加仓操作， sessionDO={}", id);
            AppAccountSessionDO sessionDO = appAccountSessionRepository.getById(id);
            if (null == sessionDO) {
                log.warn("append -> 无效的sessionId， sessionDO={}", id);
                return;
            }

            if (SessionStatusEnum.completed().contains(sessionDO.getStatus())) {
                log.warn("append -> 当前会话状态已经完结，无需再次操作， sessionId={}, status={}", id, sessionDO.getStatus());
                return;
            }
            StrategyOneMinBizParam bizParam = JSON.parseObject(sessionDO.getBizParam(), StrategyOneMinBizParam.class);
            StrategyOneMinRunParam runParam = JSON.parseObject(sessionDO.getRunParam(), StrategyOneMinRunParam.class);
            if (!statusEnum.equalsByCode(sessionDO.getBizStatus())) {
                log.warn("append -> 会话状态发生变化，无法继续执行， sessionId={}, expectStatus={}, sessionStatus={}",
                        id, statusEnum, SessionBizStatusEnum.byCode(sessionDO.getBizStatus()));
                return;
            }
            AppAccountDO accountDO = appAccountRepository.getById(sessionDO.getAccountId());

            StrategyOneMinStartSignalBO appendSignal = this.executeInner(accountDO, JSON.parseObject(sessionDO.getBaseParam(), StrategyOneMinBaseParam.class), runParam, SymbolEnum.valueOf(sessionDO.getSymbol()));
            if (null == appendSignal) {
                log.warn("append -> 当前没有明显的方向信号，直接忽略 sessionId={}, status={}", id, statusEnum);
                return;
            }
            if (!appendSignal.getPosition().equals(bizParam.getOpen().getPosition())) {
                log.warn("append -> 当前信号方向与补仓方向相反，直接忽略 sessionId={}, status={}", id, statusEnum);
                return;
            }

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
            log.warn("reverse -> 尝试开始对冲操作， sessionDO={}", id);
            AppAccountSessionDO sessionDO = appAccountSessionRepository.getById(id);
            if (null == sessionDO) {
                log.warn("reverse -> 无效的sessionId， sessionDO={}", id);
                return;
            }
            if (SessionStatusEnum.completed().contains(sessionDO.getStatus())) {
                log.warn("reverse -> 当前会话状态已经完结，无需再次操作， sessionId={}, status={}", id, sessionDO.getStatus());
                return;
            }
            StrategyOneMinBizParam bizParam = JSON.parseObject(sessionDO.getBizParam(), StrategyOneMinBizParam.class);
            if (!statusEnum.equalsByCode(sessionDO.getBizStatus())) {
                log.warn("reverse -> 会话状态发生变化，无法继续执行， sessionId={}, expectStatus={}, sessionStatus={}",
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

}
