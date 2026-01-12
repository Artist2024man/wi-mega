package com.wuin.wi_mega.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wuin.wi_mega.binance.bo.BinancePosition;
import com.wuin.wi_mega.common.cache.local.KlineCacheFactory;
import com.wuin.wi_mega.common.enums.*;
import com.wuin.wi_mega.common.exception.APIRuntimeException;
import com.wuin.wi_mega.common.exception.IResponseStatusMsg;
import com.wuin.wi_mega.common.util.LocalLock;
import com.wuin.wi_mega.model.bo.StrategyBizOrderParam;
import com.wuin.wi_mega.model.vo.TradeReqVO;
import com.wuin.wi_mega.model.vo.TradeResVO;
import com.wuin.wi_mega.model.bo.biz.StrategyMartingaleBizParam;
import com.wuin.wi_mega.model.bo.running.StrategyMartingaleRunParam;
import com.wuin.wi_mega.repository.AppAccountRepository;
import com.wuin.wi_mega.repository.AppAccountSessionRepository;
import com.wuin.wi_mega.repository.AppStrategyInstanceRepository;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppAccountOrderDO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.repository.domain.AppStrategyInstanceDO;
import com.wuin.wi_mega.repository.domain.AppUserDO;
import com.wuin.wi_mega.service.AppAccountOrderService;
import com.wuin.wi_mega.service.AppAccountService;
import com.wuin.wi_mega.service.TradeService;
import com.wuin.wi_mega.util.PermissionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
public class TradeServiceImpl implements TradeService {

    @Autowired
    private AppAccountRepository appAccountRepository;

    @Autowired
    private AppAccountSessionRepository appAccountSessionRepository;

    @Autowired
    private AppStrategyInstanceRepository appStrategyInstanceRepository;

    @Autowired
    private AppAccountOrderService appAccountOrderService;

    @Autowired
    private AppAccountService appAccountService;

    @Autowired
    private KlineCacheFactory klineCacheFactory;

    private static final String POSITION_LONG = "LONG";
    private static final String POSITION_SHORT = "SHORT";

    // ==================== 做多相关 ====================

    @Override
    public TradeResVO openLong(AppUserDO userLogin, TradeReqVO reqVO) {
        return doOpen(userLogin, reqVO, POSITION_LONG);
    }

    @Override
    public TradeResVO addLong(AppUserDO userLogin, TradeReqVO reqVO) {
        return doAdd(userLogin, reqVO, POSITION_LONG);
    }

    @Override
    public TradeResVO closeLong(AppUserDO userLogin, TradeReqVO reqVO) {
        return doClose(userLogin, reqVO, POSITION_LONG);
    }

    // ==================== 做空相关 ====================

    @Override
    public TradeResVO openShort(AppUserDO userLogin, TradeReqVO reqVO) {
        return doOpen(userLogin, reqVO, POSITION_SHORT);
    }

    @Override
    public TradeResVO addShort(AppUserDO userLogin, TradeReqVO reqVO) {
        return doAdd(userLogin, reqVO, POSITION_SHORT);
    }

    @Override
    public TradeResVO closeShort(AppUserDO userLogin, TradeReqVO reqVO) {
        return doClose(userLogin, reqVO, POSITION_SHORT);
    }

    // ==================== 其他 ====================

    @Override
    public void closeAll(AppUserDO userLogin, Long accountId) {
        AppAccountDO accountDO = getAndCheckAccount(userLogin, accountId);
        List<BinancePosition> positions = appAccountService.positionRisk(userLogin, accountId);

        if (CollectionUtils.isEmpty(positions)) {
            log.info("closeAll -> 无持仓需要平仓, accountId={}", accountId);
            return;
        }

        for (BinancePosition pos : positions) {
            if (pos.getPositionAmt() != null && pos.getPositionAmt().abs().compareTo(BigDecimal.ZERO) > 0) {
                TradeReqVO closeReq = new TradeReqVO();
                closeReq.setAccountId(accountId);
                closeReq.setQuantity(pos.getPositionAmt().abs());

                if (POSITION_LONG.equals(pos.getPositionSide())) {
                    doClose(userLogin, closeReq, POSITION_LONG);
                } else if (POSITION_SHORT.equals(pos.getPositionSide())) {
                    doClose(userLogin, closeReq, POSITION_SHORT);
                }
            }
        }
    }

    @Override
    public List<BinancePosition> getPosition(AppUserDO userLogin, Long accountId) {
        return appAccountService.positionRisk(userLogin, accountId);
    }

    // ==================== 私有方法 ====================


    /**
     * 创建手动交易会话 - 使用账号绑定的策略
     */
    private AppAccountSessionDO createManualSession(AppAccountDO accountDO, String positionSide,
                                                    BigDecimal price, BigDecimal quantity) {
        AppAccountSessionDO sessionDO = new AppAccountSessionDO();

        // 基础字段
        sessionDO.setUserId(accountDO.getUserId());
        sessionDO.setAccountId(accountDO.getId());
        sessionDO.setExchange(accountDO.getExchange());
        sessionDO.setSymbol(accountDO.getSymbol());

        // 【修复】使用账号绑定的策略实例
        AppStrategyInstanceDO instanceDO = null;
        if (accountDO.getStrategyInstanceId() != null && accountDO.getStrategyInstanceId() > 0) {
            instanceDO = appStrategyInstanceRepository.getById(accountDO.getStrategyInstanceId());
        }

        if (instanceDO != null) {
            sessionDO.setStrategyInstanceId(instanceDO.getId());
            sessionDO.setStrategyCode(instanceDO.getCode());
            sessionDO.setBaseParam(instanceDO.getBaseParam());
            sessionDO.setRunParam(instanceDO.getRunParam());
        } else {
            // 如果账号没有绑定策略，则使用默认的马丁格尔策略
            sessionDO.setStrategyInstanceId(0L);
            sessionDO.setStrategyCode(StrategyEnum.MARTINGALE_APPEND.name());
            log.warn("createManualSession -> 账号未绑定策略实例，使用默认策略, accountId={}", accountDO.getId());
        }

        // 持仓信息
        sessionDO.setHoldAvePrice(price);
        sessionDO.setHoldQty(quantity);
        sessionDO.setStatus(SessionStatusEnum.RUNNING.code());

        // 【修复】添加业务状态
        sessionDO.setBizStatus(SessionBizStatusEnum.WAIT_PROFIT.code());

        // 【修复】添加下次检查时间
        sessionDO.setNextCheckTime(LocalDateTime.now().plusSeconds(5));

        // 【修复】初始化手续费和盈亏
        sessionDO.setOpenFee(BigDecimal.ZERO);
        sessionDO.setCloseFee(BigDecimal.ZERO);
        sessionDO.setClosePnl(BigDecimal.ZERO);

        // 【修复】设置同步状态
        sessionDO.setSyncStatus(SyncStatusEnum.NO_SYNC.getCode());

        // 【修复】设置模拟/真实交易标记
        sessionDO.setMockData(TradeTypeEnum.MOCK.equalsByCode(accountDO.getTradeType())
                ? MockDataEnum.MOCK.code() : MockDataEnum.REAL.code());

        // 【修复】构建完整的 bizParam，兼容马丁格尔策略
        StrategyMartingaleBizParam bizParam = new StrategyMartingaleBizParam();
        bizParam.getOpen().setPosition(positionSide);
        bizParam.getOpen().setPrice(price);
        bizParam.getOpen().setQty(quantity);
        bizParam.getOpen().setAvgPrice(price);

        // 从runParam获取止盈止损等参数
        if (instanceDO != null && instanceDO.getRunParam() != null) {
            try {
                StrategyMartingaleRunParam runParam = JSON.parseObject(instanceDO.getRunParam(),
                        StrategyMartingaleRunParam.class);
                // 设置止盈止损价格
                if (runParam.getTp() != null && runParam.getTp().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal takeProfitPrice = POSITION_LONG.equals(positionSide)
                            ? price.add(runParam.getTp())
                            : price.subtract(runParam.getTp());
                    sessionDO.setTakeProfitPrice(takeProfitPrice);
                }
                if (runParam.getSl() != null && runParam.getSl().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal stopLossPrice = POSITION_LONG.equals(positionSide)
                            ? price.subtract(runParam.getSl())
                            : price.add(runParam.getSl());
                    sessionDO.setStopLossPrice(stopLossPrice);
                }

                if (CollectionUtils.isNotEmpty(runParam.getAppends())) {
                    for (int i = 0; i < runParam.getAppends().size(); i++) {
                        StrategyMartingaleRunParam.QtyPriceBO appBO = runParam.getAppends().get(i);
                        StrategyBizOrderParam append = new StrategyBizOrderParam(i, positionSide, appBO.getQty());
                        if (i == 0) { //首次开仓需要填充第一次补仓数据
                            append.setPrice(POSITION_LONG.equals(positionSide) ? price.subtract(appBO.getLos()) : price.add(appBO.getLos()));
                        }
                        bizParam.getAppends().add(append);
                    }
                }

                if (null != runParam.getRslos()) {
                    bizParam.setReverse(new StrategyBizOrderParam());
                    bizParam.getReverse().setPosition(POSITION_LONG.equals(positionSide) ? "SHORT" : "LONG");
                    bizParam.getReverse().setQty(runParam.getOpqty());
                }
            } catch (Exception e) {
                log.warn("createManualSession -> 解析runParam失败, accountId={}, error={}",
                        accountDO.getId(), e.getMessage());
            }
        }

        sessionDO.setBizParam(JSON.toJSONString(bizParam));

        // 时间字段
        sessionDO.setCreateTime(LocalDateTime.now());
        sessionDO.setUpdateTime(LocalDateTime.now());

        appAccountSessionRepository.save(sessionDO);
        log.info("createManualSession -> 创建手动交易会话, accountId={}, sessionId={}, position={}, strategyCode={}, tp={}, sl={}",
                accountDO.getId(), sessionDO.getId(), positionSide, sessionDO.getStrategyCode(),
                sessionDO.getTakeProfitPrice(), sessionDO.getStopLossPrice());

        return sessionDO;
    }


    /**
     * 加仓 - 更新止盈止损价格和业务参数
     */
    private TradeResVO doAdd(AppUserDO userLogin, TradeReqVO reqVO, String positionSide) {
        validateQuantity(reqVO.getQuantity());
        AppAccountDO accountDO = getAndCheckAccount(userLogin, reqVO.getAccountId());

        // 查找该方向的会话
        AppAccountSessionDO sessionDO = findRunningSessionByPosition(accountDO.getId(), positionSide);
        if (sessionDO == null || sessionDO.getHoldQty() == null || sessionDO.getHoldQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR,
                    positionSide.equals(POSITION_LONG) ? "无多头持仓，请先开仓" : "无空头持仓，请先开仓");
        }

        BigDecimal curPrice = getCurrentPrice(accountDO.getSymbol());

        return LocalLock.execute("TRADE_" + reqVO.getAccountId(), () -> {
            AppAccountOrderDO orderDO = appAccountOrderService.openMarket(
                    null, accountDO, sessionDO.getId(), positionSide, curPrice, reqVO.getQuantity());

            // 计算新的均价和总数量
            BigDecimal oldAmount = sessionDO.getHoldAvePrice().multiply(sessionDO.getHoldQty());
            BigDecimal newAmount = curPrice.multiply(reqVO.getQuantity());
            BigDecimal totalQty = sessionDO.getHoldQty().add(reqVO.getQuantity());
            BigDecimal avgPrice = oldAmount.add(newAmount).divide(totalQty, 6, RoundingMode.HALF_UP);

            AppAccountSessionDO update = new AppAccountSessionDO();
            update.setId(sessionDO.getId());
            update.setHoldAvePrice(avgPrice);
            update.setHoldQty(totalQty);
            update.setUpdateTime(LocalDateTime.now());

            // 【修复】更新止盈止损价格
            updateTakeProfitAndStopLoss(update, sessionDO, avgPrice, positionSide);

            // 【修复】更新业务参数中的加仓信息
            updateBizParamForAdd(update, sessionDO, curPrice, reqVO.getQuantity(), orderDO.getId(), avgPrice, totalQty, positionSide);

            appAccountSessionRepository.updateById(update);

            log.info("doAdd -> 加仓成功, accountId={}, position={}, qty={}, totalQty={}, avgPrice={}, newTp={}, newSl={}",
                    reqVO.getAccountId(), positionSide, reqVO.getQuantity(), totalQty, avgPrice,
                    update.getTakeProfitPrice(), update.getStopLossPrice());

            return buildTradeRes(orderDO, positionSide, "ADD", "加仓成功");
        });
    }

    /**
     * 【新增】根据新均价更新止盈止损价格
     */
    private void updateTakeProfitAndStopLoss(AppAccountSessionDO update, AppAccountSessionDO sessionDO,
                                             BigDecimal newAvgPrice, String positionSide) {
        BigDecimal minTp = null;
        BigDecimal sl = null;

        // 方式1：从runParam获取配置（策略建仓的情况）
        if (sessionDO.getRunParam() != null && !sessionDO.getRunParam().isEmpty()) {
            try {
                StrategyMartingaleRunParam runParam = JSON.parseObject(sessionDO.getRunParam(),
                        StrategyMartingaleRunParam.class);
                minTp = runParam.getTp();
                sl = runParam.getSl();
            } catch (Exception e) {
                log.debug("updateTakeProfitAndStopLoss -> 解析runParam失败");
            }
        }

        // 方式2：从原有止盈止损价格推算差值
        if (minTp == null && sessionDO.getTakeProfitPrice() != null && sessionDO.getHoldAvePrice() != null) {
            minTp = sessionDO.getTakeProfitPrice().subtract(sessionDO.getHoldAvePrice()).abs();
        }
        if (sl == null && sessionDO.getStopLossPrice() != null && sessionDO.getHoldAvePrice() != null) {
            sl = sessionDO.getHoldAvePrice().subtract(sessionDO.getStopLossPrice()).abs();
        }

        // 基于新均价计算新的止盈止损价格
        if (minTp != null && minTp.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal takeProfitPrice = POSITION_LONG.equals(positionSide)
                    ? newAvgPrice.add(minTp)
                    : newAvgPrice.subtract(minTp);
            update.setTakeProfitPrice(takeProfitPrice);
        }

        if (sl != null && sl.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal stopLossPrice = POSITION_LONG.equals(positionSide)
                    ? newAvgPrice.subtract(sl)
                    : newAvgPrice.add(sl);
            update.setStopLossPrice(stopLossPrice);
        }
    }

    /**
     * 【新增】更新业务参数中的加仓信息
     */
    private void updateBizParamForAdd(AppAccountSessionDO update, AppAccountSessionDO sessionDO,
                                      BigDecimal addPrice, BigDecimal addQty, Long orderId,
                                      BigDecimal newAvgPrice, BigDecimal totalQty, String positionSide) {
        StrategyMartingaleBizParam bizParam;

//        if (sessionDO.getBizParam() != null && !sessionDO.getBizParam().isEmpty()) {
//            try {
//                bizParam = JSON.parseObject(sessionDO.getBizParam(), StrategyMartingaleParam.class);
//            } catch (Exception e) {
//                bizParam = new StrategyMartingaleParam();
//                bizParam.setOpenPosition(positionSide);
//                log.warn("updateBizParamForAdd -> 解析bizParam失败, 创建新的bizParam, sessionId={}", sessionDO.getId());
//            }
//        } else {
//            bizParam = new StrategyMartingaleParam();
//            bizParam.setOpenPosition(positionSide);
//        }
//
//        // 判断是第几次加仓
//        boolean isFirstAppend = bizParam.getAppendOrderId1() == null;
//        boolean isSecondAppend = !isFirstAppend && bizParam.getAppendOrderId2() == null;
//
//        StrategyMartingaleRunParam runParam = null;
//        if (sessionDO.getRunParam() != null && !sessionDO.getRunParam().isEmpty()) {
//            try {
//                runParam = JSON.parseObject(sessionDO.getRunParam(), StrategyMartingaleRunParam.class);
//            } catch (Exception e) {
//                log.debug("updateBizParamForAdd -> 解析runParam失败");
//            }
//        }
//
//        if (isFirstAppend) {
//            // 第一次加仓
//            bizParam.setAppendOrderId1(orderId);
//            bizParam.setAveAppendPrice1(addPrice);
//            bizParam.setAppendQty1(addQty);
//            update.setBizStatus(SessionBizStatusEnum.WAIT_APPEND_PROFIT.code());
//
//            // 设置第二次补仓价格
//            if (runParam != null && runParam.getAplos2() != null && runParam.getAplos2().compareTo(BigDecimal.ZERO) > 0) {
//                bizParam.setAppendPrice2(POSITION_LONG.equals(positionSide)
//                        ? newAvgPrice.subtract(runParam.getAplos2())
//                        : newAvgPrice.add(runParam.getAplos2()));
//            }
//
//            // 设置对冲价格
//            if (runParam != null && runParam.getRslos() != null && runParam.getRslos().compareTo(BigDecimal.ZERO) > 0) {
//                bizParam.setReversePrice(POSITION_LONG.equals(positionSide)
//                        ? newAvgPrice.subtract(runParam.getRslos())
//                        : newAvgPrice.add(runParam.getRslos()));
//            }
//            bizParam.setReverseQty(totalQty);
//
//        } else if (isSecondAppend) {
//            // 第二次加仓
//            bizParam.setAppendOrderId2(orderId);
//            bizParam.setAveAppendPrice2(addPrice);
//            bizParam.setAppendQty2(addQty);
//            update.setBizStatus(SessionBizStatusEnum.WAIT_APPEND_2_PROFIT.code());
//
//            // 更新对冲价格
//            if (runParam != null && runParam.getRslos() != null && runParam.getRslos().compareTo(BigDecimal.ZERO) > 0) {
//                bizParam.setReversePrice(POSITION_LONG.equals(positionSide)
//                        ? newAvgPrice.subtract(runParam.getRslos())
//                        : newAvgPrice.add(runParam.getRslos()));
//            }
//            bizParam.setReverseQty(totalQty);
//        }

//        update.setBizParam(JSON.toJSONString(bizParam));
    }

    /**
     * 平仓
     */
    private TradeResVO doClose(AppUserDO userLogin, TradeReqVO reqVO, String positionSide) {
        AppAccountDO accountDO = getAndCheckAccount(userLogin, reqVO.getAccountId());

        // 查找该方向的会话
        AppAccountSessionDO sessionDO = findRunningSessionByPosition(accountDO.getId(), positionSide);
        if (sessionDO == null || sessionDO.getHoldQty() == null || sessionDO.getHoldQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR,
                    positionSide.equals(POSITION_LONG) ? "无多头持仓可平" : "无空头持仓可平");
        }

        // 确定平仓数量
        BigDecimal closeQty = reqVO.getQuantity();
        if (closeQty == null || closeQty.compareTo(BigDecimal.ZERO) <= 0) {
            closeQty = sessionDO.getHoldQty(); // 全部平仓
        }
        if (closeQty.compareTo(sessionDO.getHoldQty()) > 0) {
            closeQty = sessionDO.getHoldQty(); // 不能超过持仓数量
        }

        BigDecimal curPrice = getCurrentPrice(accountDO.getSymbol());
        final BigDecimal finalCloseQty = closeQty;

        return LocalLock.execute("TRADE_" + reqVO.getAccountId(), () -> {
            AppAccountOrderDO orderDO = appAccountOrderService.closeMarket(
                    null, accountDO, sessionDO, positionSide, curPrice, finalCloseQty);

            // 更新会话持仓信息
            BigDecimal remainQty = sessionDO.getHoldQty().subtract(finalCloseQty);
            AppAccountSessionDO update = new AppAccountSessionDO();
            update.setId(sessionDO.getId());
            update.setUpdateTime(LocalDateTime.now());

            if (remainQty.compareTo(BigDecimal.ZERO) <= 0) {
                // 全部平仓，更新会话状态
                update.setStatus(SessionStatusEnum.STOP_HAND.code());
                update.setHoldQty(BigDecimal.ZERO);
                log.info("doClose -> 全部平仓，会话结束, sessionId={}", sessionDO.getId());
            } else {
                // 部分平仓，更新持仓数量
                update.setHoldQty(remainQty);
                log.info("doClose -> 部分平仓, sessionId={}, remainQty={}", sessionDO.getId(), remainQty);
            }
            appAccountSessionRepository.updateById(update);

            log.info("doClose -> 平仓成功, accountId={}, position={}, closeQty={}, remainQty={}, price={}",
                    reqVO.getAccountId(), positionSide, finalCloseQty, remainQty, curPrice);

            return buildTradeRes(orderDO, positionSide, "CLOSE", "平仓成功");
        });
    }

    /**
     * 开仓（新建仓位）- 修复开空问题
     */
    private TradeResVO doOpen(AppUserDO userLogin, TradeReqVO reqVO, String positionSide) {
        validateQuantity(reqVO.getQuantity());
        AppAccountDO accountDO = getAndCheckAccount(userLogin, reqVO.getAccountId());

        // 检查是否已有该方向的持仓（只检查同方向，不同方向可以同时存在）
        AppAccountSessionDO existSession = findRunningSessionByPosition(accountDO.getId(), positionSide);
        if (existSession != null && existSession.getHoldQty() != null && existSession.getHoldQty().compareTo(BigDecimal.ZERO) > 0) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR,
                    positionSide.equals(POSITION_LONG) ? "已有多头持仓，请使用加仓接口" : "已有空头持仓，请使用加仓接口");
        }

        BigDecimal curPrice = getCurrentPrice(accountDO.getSymbol());

        return LocalLock.execute("TRADE_" + reqVO.getAccountId(), () -> {
            // 创建会话记录
            AppAccountSessionDO sessionDO = createManualSession(accountDO, positionSide, curPrice, reqVO.getQuantity());

            AppAccountOrderDO orderDO = appAccountOrderService.openMarket(
                    null, accountDO, sessionDO.getId(), positionSide, curPrice, reqVO.getQuantity());

            log.info("doOpen -> 开仓成功, accountId={}, position={}, qty={}, price={}, sessionId={}",
                    reqVO.getAccountId(), positionSide, reqVO.getQuantity(), curPrice, sessionDO.getId());

            return buildTradeRes(orderDO, positionSide, "OPEN", "开仓成功");
        });
    }


    /**
     * 获取并检查账号
     */
    private AppAccountDO getAndCheckAccount(AppUserDO userLogin, Long accountId) {
        if (accountId == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR, "账号ID不能为空");
        }
        AppAccountDO accountDO = appAccountRepository.getById(accountId);
        if (accountDO == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_ID_NOT_EXIST);
        }
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());
        return accountDO;
    }

    /**
     * 校验数量
     */
    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR, "交易数量必须大于0");
        }
    }

    /**
     * 获取当前价格
     */
    private BigDecimal getCurrentPrice(String symbol) {
        return klineCacheFactory.getCurPrice(SymbolEnum.valueOf(symbol), KlineIntervalEnum.MINUTE_1);
    }

    /**
     * 查找指定方向的持仓
     */
    private BinancePosition findPosition(List<BinancePosition> positions, String positionSide) {
        if (CollectionUtils.isEmpty(positions)) {
            return null;
        }
        return positions.stream()
                .filter(p -> positionSide.equals(p.getPositionSide()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据持仓方向查找运行中的会话
     */
    private AppAccountSessionDO findRunningSessionByPosition(Long accountId, String positionSide) {
        List<AppAccountSessionDO> runningSessions = appAccountSessionRepository
                .listByAccountIdAndStatusList(accountId, SessionStatusEnum.running());

        if (CollectionUtils.isEmpty(runningSessions)) {
            return null;
        }

        for (AppAccountSessionDO session : runningSessions) {
            if (session.getHoldQty() != null && session.getHoldQty().compareTo(BigDecimal.ZERO) > 0) {
                // 解析持仓方向
                String sessionPosition = parsePositionSide(session);
                if (positionSide.equals(sessionPosition)) {
                    return session;
                }
            }
        }
        return null;
    }

    /**
     * 从会话的bizParam中解析持仓方向
     */
    private String parsePositionSide(AppAccountSessionDO session) {
        if (session.getBizParam() == null || session.getBizParam().isEmpty()) {
            return null;
        }
        try {
            JSONObject bizParamJson = JSON.parseObject(session.getBizParam());
            String openPosition = bizParamJson.getString("openPosition");
            if (openPosition != null && !openPosition.isEmpty()) {
                return openPosition;
            }
            String positionSide = bizParamJson.getString("positionSide");
            if (positionSide != null && !positionSide.isEmpty()) {
                return positionSide;
            }
        } catch (Exception e) {
            log.warn("parsePositionSide -> 解析bizParam失败, sessionId={}", session.getId());
        }
        return null;
    }

    /**
     * 更新会话持仓信息
     */
    private void updateSessionHolding(AppAccountSessionDO sessionDO, AppAccountOrderDO orderDO) {
        AppAccountSessionDO update = new AppAccountSessionDO();
        update.setId(sessionDO.getId());

        BigDecimal oldAmount = sessionDO.getHoldAvePrice().multiply(sessionDO.getHoldQty());
        BigDecimal newAmount = orderDO.getAvePrice().multiply(orderDO.getQty());
        BigDecimal totalQty = sessionDO.getHoldQty().add(orderDO.getQty());
        BigDecimal avgPrice = oldAmount.add(newAmount).divide(totalQty, 6, RoundingMode.HALF_UP);

        update.setHoldAvePrice(avgPrice);
        update.setHoldQty(totalQty);
        appAccountSessionRepository.updateById(update);
    }

    /**
     * 构建交易结果
     */
    private TradeResVO buildTradeRes(AppAccountOrderDO orderDO, String positionSide, String orderType, String message) {
        return TradeResVO.builder()
                .orderId(orderDO.getOrderId())
                .clientOrderId(orderDO.getClientOrderId())
                .symbol(orderDO.getSymbol())
                .positionSide(positionSide)
                .orderType(orderType)
                .quantity(orderDO.getQty())
                .avgPrice(orderDO.getAvePrice())
                .amount(orderDO.getQty().multiply(orderDO.getAvePrice()))
                .fee(orderDO.getFee())
                .sessionId(orderDO.getSessionId())
                .success(true)
                .message(message)
                .build();
    }
}
