package com.wuin.wi_mega.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wuin.wi_mega.binance.bo.BinanceAlgoOrderDTO;
import com.wuin.wi_mega.binance.bo.BinanceCommissionRateDTO;
import com.wuin.wi_mega.binance.bo.BinancePosition;
import com.wuin.wi_mega.binance.bo.FuturesAccountAssetDTO;
import com.wuin.wi_mega.common.cache.local.KlineCacheFactory;
import com.wuin.wi_mega.common.enums.*;
import com.wuin.wi_mega.common.exception.APIRuntimeException;
import com.wuin.wi_mega.common.exception.IResponseStatusMsg;
import com.wuin.wi_mega.common.util.DateUtils;
import com.wuin.wi_mega.common.util.LocalLock;
import com.wuin.wi_mega.model.PageRequestVO;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.bo.AccountEquityAddBO;
import com.wuin.wi_mega.model.bo.SessionAmtBO;
import com.wuin.wi_mega.model.bo.StrategyBizOrderParam;
import com.wuin.wi_mega.model.bo.StrategyStatBO;
import com.wuin.wi_mega.model.bo.biz.StrategyBizParam;
import com.wuin.wi_mega.model.vo.*;
import com.wuin.wi_mega.repository.*;
import com.wuin.wi_mega.repository.domain.*;
import com.wuin.wi_mega.service.AccountEquityService;
import com.wuin.wi_mega.service.AppAccountOrderService;
import com.wuin.wi_mega.service.AppAccountService;
import com.wuin.wi_mega.service.StrategyService;
import com.wuin.wi_mega.util.PermissionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AppAccountServiceImpl implements AppAccountService {

    @Autowired
    private AppAccountRepository appAccountRepository;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private AppAccountOrderRepository appAccountOrderRepository;

    @Autowired
    private StatAccountEquityLogMinuteRepository accountEquityLogMinuteRepository;
    @Autowired
    private StatAccountEquityLogHourRepository accountEquityLogHourRepository;
    @Autowired
    private StatAccountEquityLogDayRepository accountEquityLogDayRepository;

    @Autowired
    private AccountEquityService accountEquityService;

    @Autowired
    private AppStrategyInstanceRepository appStrategyInstanceRepository;

    @Autowired
    private AppAccountSessionRepository appAccountSessionRepository;

    @Autowired
    private AppAccountOrderService appAccountOrderService;

    @Autowired
    private KlineCacheFactory klineCacheFactory;


    @Override
    public List<AppAccountDO> listByStatus(StrategyStatusEnum strategyStatus) {
        return appAccountRepository.listByStatus(strategyStatus.getCode());
    }

    @Override
    public Long create(AppUserDO userLogin, AccountCreateReqVO reqVO) {
        // 参数校验
        if (reqVO.getStrategyInstanceId() == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR, "strategyInstanceId不能为空");
        }

        // 获取交易模式，默认实盘
        Integer tradeType = reqVO.getTradeType();

        //要验证API Key
        if (StringUtils.isBlank(reqVO.getApiKey())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR, "KEY不能为空");
        }
        if (StringUtils.isBlank(reqVO.getApiKeyPass())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR, "SECRET不能为空");
        }

        // 检查API Key是否已存在（仅实盘模式）
        if (TradeTypeEnum.REAL.equalsByCode(tradeType) && StringUtils.isNotBlank(reqVO.getApiKey())) {
            AppAccountDO existAccount = appAccountRepository.getByApiKey(reqVO.getApiKey());
            if (null != existAccount) {
                throw new APIRuntimeException(IResponseStatusMsg.APIEnum.KEY_EXIST);
            }
        }

        AppStrategyInstanceDO instanceDO = appStrategyInstanceRepository.getById(reqVO.getStrategyInstanceId());
        if (null == instanceDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.INVALID_STRATEGY_INSTANCE_ID);
        }

        AppAccountDO accountDO = new AppAccountDO();
        accountDO.setName(reqVO.getName());
        accountDO.setUserId(userLogin.getId());
        accountDO.setApiKey(reqVO.getApiKey());
        accountDO.setExchange(reqVO.getExchange());
        accountDO.setApiKeyPass(reqVO.getApiKeyPass());
        accountDO.setStrategyInstanceId(instanceDO.getId());
        accountDO.setSymbol(instanceDO.getSymbol());
        accountDO.setStrategyStatus(StrategyStatusEnum.STOP.code());
        accountDO.setRemark(reqVO.getRemark());
        accountDO.setInitEquity(reqVO.getInitEquity());
        accountDO.setCurEquity(reqVO.getInitEquity());
        accountDO.setEquityCoin("USDT");
        accountDO.setNextSyncTime(LocalDateTime.now());

        accountDO.setDualSidePosition(reqVO.getDualSidePosition());
        accountDO.setLeverage(reqVO.getLeverage());
        accountDO.setCloseFee(BigDecimal.ZERO);
        accountDO.setOpenFee(BigDecimal.ZERO);
        accountDO.setClosePnl(BigDecimal.ZERO);
        accountDO.setStrategyMinPrice(BigDecimal.ZERO);
        accountDO.setStrategyMaxPrice(BigDecimal.ZERO);
        accountDO.setTakerFeeRate(BigDecimal.valueOf(0.0002));
        accountDO.setMakerFeeRate(BigDecimal.valueOf(0.0005));
        accountDO.setLastSyncSessionId(0L);
        accountDO.setTradeType(reqVO.getTradeType());
        // 实盘模式：设置持仓模式和杠杆
        try {
            Boolean dbIsDualSidePosition = DualSidePositionEnum.DOUBLE.equalsByCode(accountDO.getDualSidePosition());
            Boolean isDualSidePosition = accountDO.fetchDualSidePosition();
            if (!dbIsDualSidePosition.equals(isDualSidePosition)) {
                String res = accountDO.changePositionMode(dbIsDualSidePosition);
                log.warn("create -> 当前持仓模式和期望持仓模式不一致，current={}, expect={}, resp={}", isDualSidePosition, dbIsDualSidePosition, res);
            }
            String leverRes = accountDO.changeLeverage(accountDO.getLeverage());
            log.warn("create -> 修改当前用户杠杆比例，leverage={}, resp={}", accountDO.getLeverage(), leverRes);
        } catch (Throwable t) {
            log.error("create -> exception", t);
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.API_KEY_INVALID);
        }
        BinanceCommissionRateDTO rateDTO = accountDO.feeRate();
        if (null != rateDTO) {
            accountDO.setTakerFeeRate(rateDTO.getTakerCommissionRate());
            accountDO.setMakerFeeRate(rateDTO.getMakerCommissionRate());
        }
        appAccountRepository.save(accountDO);
        // 如果 autoStart 为 true，则启动策略
        if (Boolean.TRUE.equals(reqVO.getAutoStart())) {
            AccountReqVO startReq = new AccountReqVO();
            startReq.setAccountId(accountDO.getId());
            this.start(userLogin, startReq);
        }
        return accountDO.getId();
    }


    /**
     * 创建账号后自动启动策略
     *
     * @param accountDO 账号信息
     */
    private void autoStartStrategy(AppAccountDO accountDO) {
        try {
            // 检查是否已有持仓（区分模拟/实盘）
            List<BinancePosition> positionList;
            if (TradeTypeEnum.MOCK.equalsByCode(accountDO.getTradeType())) {
                positionList = buildMockPositionRisk(accountDO);
            } else {
                positionList = accountDO.positionRisk();
            }
            if (CollectionUtils.isNotEmpty(positionList)) {
                log.warn("autoStartStrategy -> 账号已有持仓，跳过自动启动, accountId={}", accountDO.getId());
                return;
            }

            // 更新状态为运行中
            AppAccountDO update = new AppAccountDO();
            update.setId(accountDO.getId());
            update.setStrategyStatus(StrategyStatusEnum.RUNNING.code());
            appAccountRepository.updateById(update);
            log.info("autoStartStrategy -> 账号创建后自动启动成功, accountId={}", accountDO.getId());
        } catch (Exception e) {
            log.error("autoStartStrategy -> 账号创建后自动启动失败, accountId={}, error={}", accountDO.getId(), e.getMessage(), e);
        }
    }


    @Override
    public void update(AppUserDO userLogin, AccountUpdateReqVO reqVO) {
        if (reqVO.getStrategyInstanceId() == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR, "strategyInstanceId不能为空");
        }

        AppAccountDO accountDO = appAccountRepository.getById(reqVO.getId());
        if (null == accountDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_ID_NOT_EXIST);
        }
        if (StrategyStatusEnum.RUNNING.equalsByCode(accountDO.getStrategyStatus())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.STRATEGY_IS_RUNNING);
        }
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());

        AppStrategyInstanceDO instanceDO = appStrategyInstanceRepository.getById(reqVO.getStrategyInstanceId());
        if (null == instanceDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.INVALID_STRATEGY_INSTANCE_ID);
        }

        // 获取交易模式（如果传了则使用新值，否则保持原值）
        Integer tradeType = reqVO.getTradeType() != null ? reqVO.getTradeType() : accountDO.getTradeType();

        AppAccountDO tempAccount = null;
        // 实盘模式才需要验证API Key
        if (TradeTypeEnum.REAL.equalsByCode(tradeType)) {
            if (StringUtils.isBlank(reqVO.getApiKey())) {
                throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR, "apiKey不能为空");
            }
            if (StringUtils.isBlank(reqVO.getApiKeyPass())) {
                throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR, "apiKeyPass/apiSecret不能为空");
            }

            // 验证API Key有效性
            tempAccount = new AppAccountDO();
            tempAccount.setApiKey(reqVO.getApiKey());
            tempAccount.setApiKeyPass(reqVO.getApiKeyPass());
            tempAccount.setSymbol(instanceDO.getSymbol());
            try {
                tempAccount.fetchOpenOrders();
            } catch (Exception e) {
                log.error("update -> API Key验证失败, error={}", e.getMessage());
                throw new APIRuntimeException(IResponseStatusMsg.APIEnum.API_KEY_INVALID);
            }
        } else {
            log.info("update -> 模拟模式，跳过API Key验证, accountId={}", accountDO.getId());
        }

        AppAccountDO update = new AppAccountDO();
        update.setId(accountDO.getId());
        update.setName(reqVO.getName());
        update.setStrategyInstanceId(reqVO.getStrategyInstanceId());
        update.setApiKey(reqVO.getApiKey());
        update.setApiKeyPass(reqVO.getApiKeyPass());
        update.setSymbol(instanceDO.getSymbol());
        update.setDualSidePosition(reqVO.getDualSidePosition());
        update.setLeverage(reqVO.getLeverage());
        if (reqVO.getTradeType() != null) {
            update.setTradeType(reqVO.getTradeType());
        }

        // 实盘模式：设置持仓模式和杠杆
        if (TradeTypeEnum.REAL.equalsByCode(tradeType) && tempAccount != null) {
            Boolean dbIsDualSidePosition = DualSidePositionEnum.DOUBLE.equalsByCode(reqVO.getDualSidePosition());
            Boolean isDualSidePosition = tempAccount.fetchDualSidePosition();
            if (!dbIsDualSidePosition.equals(isDualSidePosition)) {
                String res = tempAccount.changePositionMode(dbIsDualSidePosition);
                log.warn("update -> 当前持仓模式和期望持仓模式不一致，current={}, expect={}, resp={}", isDualSidePosition, dbIsDualSidePosition, res);
            }
            String leverRes = tempAccount.changeLeverage(reqVO.getLeverage());
            log.warn("update -> 修改当前用户杠杆比例，leverage={}, resp={}", reqVO.getLeverage(), leverRes);
        }

        appAccountRepository.updateById(update);
    }


    @Override
    public void start(AppUserDO userLogin, AccountReqVO reqVO) {
        AppAccountDO accountDO = appAccountRepository.getById(reqVO.getAccountId());
        if (null == accountDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_ID_NOT_EXIST);
        }
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());
        if (StrategyStatusEnum.RUNNING.equalsByCode(accountDO.getStrategyStatus())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.STRATEGY_IS_RUNNING);
        }
        if (null == accountDO.getStrategyInstanceId()) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.HAS_NO_STRATEGY);
        }
        if (null == appStrategyInstanceRepository.getById(accountDO.getStrategyInstanceId())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.INVALID_STRATEGY_INSTANCE_ID);
        }
        // 检查是否已有持仓
        SessionMobResVO runningSession = this.runningSession(userLogin, accountDO.getId());
        if (CollectionUtils.isNotEmpty(runningSession.getPositions())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ALREADY_HAS_POSITION);
        }
        // 实盘模式才需要设置持仓模式和杠杆
        if (TradeTypeEnum.REAL.equalsByCode(accountDO.getTradeType())) {
            Boolean dbIsDualSidePosition = DualSidePositionEnum.DOUBLE.equalsByCode(accountDO.getDualSidePosition());
            Boolean isDualSidePosition = accountDO.fetchDualSidePosition();
            if (!dbIsDualSidePosition.equals(isDualSidePosition)) {
                String res = accountDO.changePositionMode(dbIsDualSidePosition);
                log.warn("start -> 当前持仓模式和期望持仓模式不一致，强制修改持仓模式，current={}, expect={}, resp={}", isDualSidePosition, dbIsDualSidePosition, res);
            }
            String leverRes = accountDO.changeLeverage(accountDO.getLeverage());
            log.warn("start -> 修改当前用户杠杆比例，leverage={}, resp={}", accountDO.getLeverage(), leverRes);
        }
        AppAccountDO update = new AppAccountDO();
        update.setId(accountDO.getId());
        update.setStrategyStatus(StrategyStatusEnum.RUNNING.code());
        update.setStrategyMinPrice(BigDecimal.ZERO);
        update.setStrategyMaxPrice(BigDecimal.ZERO);
        if (null != reqVO.getStrategyMinPrice()) {
            update.setStrategyMinPrice(reqVO.getStrategyMinPrice());
        }
        if (null != reqVO.getStrategyMaxPrice()) {
            update.setStrategyMaxPrice(reqVO.getStrategyMaxPrice());
        }
        appAccountRepository.updateById(update);
    }


    @Override
    public void stop(AppUserDO userLogin, AccountReqVO reqVO) {
        AppAccountDO accountDO = appAccountRepository.getById(reqVO.getAccountId());
        if (null == accountDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_ID_NOT_EXIST);
        }
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());
        if (StrategyStatusEnum.STOP.equalsByCode(accountDO.getStrategyStatus())) {
            return;
        }
        strategyService.stop(accountDO);
        AppAccountDO update = new AppAccountDO();
        update.setId(accountDO.getId());
        update.setStrategyStatus(StrategyStatusEnum.STOP.code());
        appAccountRepository.updateById(update);
    }

    @Override
    public void delete(AppUserDO userLogin, Long id) {
        AppAccountDO accountDO = appAccountRepository.getById(id);
        if (null == accountDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_ID_NOT_EXIST);
        }
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());
        if (StrategyStatusEnum.RUNNING.equalsByCode(accountDO.getStrategyStatus())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.STRATEGY_IS_RUNNING);
        }
        appAccountRepository.removeById(accountDO.getId());
    }

    @Override
    public PageResponseVO<AccountResVO> pageList(AppUserDO userLogin, PageRequestVO<?> requestVO) {
        IPage<AppAccountDO> page = appAccountRepository.pageListByUserId(userLogin.getId(), requestVO.getPage(), requestVO.getPageSize());
        PageResponseVO<AccountResVO> pageRes = new PageResponseVO<>();
        log.warn("pageList -> page={}", JSON.toJSONString(page));
        pageRes.setTotal(page.getTotal());

        if (CollectionUtils.isNotEmpty(page.getRecords())) {

            Set<Long> instanceIds = page.getRecords().stream().map(AppAccountDO::getStrategyInstanceId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());

            Map<Long, AppStrategyInstanceDO> instanceDOMap = new HashMap<>();

            if (CollectionUtils.isNotEmpty(instanceIds)) {
                List<AppStrategyInstanceDO> instanceList = appStrategyInstanceRepository.listByIds(instanceIds);
                if (CollectionUtils.isNotEmpty(instanceList)) {
                    instanceDOMap = instanceList.stream().collect(Collectors.toMap(AppStrategyInstanceDO::getId, a -> a));
                }
            }

            for (AppAccountDO record : page.getRecords()) {
                AccountResVO resVO = new AccountResVO(record);
                AppStrategyInstanceDO ins = instanceDOMap.get(record.getStrategyInstanceId());
                if (null != ins) {
                    resVO.setStrategyCode(ins.getCode());
                    resVO.setStrategyInstanceName(ins.getName());
                }
                pageRes.getRecords().add(resVO);
            }
        }
        return pageRes;
    }

    @Override
    public List<BinancePosition> positionRisk(AppUserDO userLogin, Long accountId) {
        AppAccountDO accountDO = appAccountRepository.getById(accountId);
        if (null == accountDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_ID_NOT_EXIST);
        }
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());

        // 模拟模式：从本地会话数据构建模拟持仓
        if (TradeTypeEnum.MOCK.equalsByCode(accountDO.getTradeType())) {
            return buildMockPositionRisk(accountDO);
        }

        // 实盘模式：调用币安API查询真实持仓
        return accountDO.positionRisk();
    }

    /**
     * 构建模拟持仓数据
     * 从本地会话表中读取运行中的会话，构建模拟的BinancePosition
     */
    private List<BinancePosition> buildMockPositionRisk(AppAccountDO accountDO) {
        List<BinancePosition> positions = new ArrayList<>();

        // 查询该账户所有运行中的会话
        List<AppAccountSessionDO> runningSessions = appAccountSessionRepository
                .listByAccountIdAndStatusList(accountDO.getId(), SessionStatusEnum.running());

        if (CollectionUtils.isEmpty(runningSessions)) {
            return positions;
        }

        // 获取当前市场价格用于计算未实现盈亏
        SymbolEnum symbolEnum = SymbolEnum.valueOf(accountDO.getSymbol());
        BigDecimal currentPrice = klineCacheFactory.getCurPrice(symbolEnum, KlineIntervalEnum.MINUTE_1);
        if (currentPrice == null) {
            currentPrice = BigDecimal.ZERO;
        }

        // 获取杠杆倍数，默认20倍
        int leverage = accountDO.getLeverage() != null ? accountDO.getLeverage() : 20;

        for (AppAccountSessionDO session : runningSessions) {
            // 跳过没有持仓的会话
            if (session.getHoldQty() == null || session.getHoldQty().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 解析业务参数获取持仓方向
            String positionSide = parsePositionSide(session);
            if (StringUtils.isBlank(positionSide)) {
                continue;
            }

            BigDecimal holdQty = session.getHoldQty();
            BigDecimal entryPrice = session.getHoldAvePrice() != null ? session.getHoldAvePrice() : BigDecimal.ZERO;

            // 计算未实现盈亏
            BigDecimal unRealizedProfit = BigDecimal.ZERO;
            if (currentPrice.compareTo(BigDecimal.ZERO) > 0 && entryPrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal priceDiff = currentPrice.subtract(entryPrice);
                if ("SHORT".equals(positionSide)) {
                    priceDiff = priceDiff.negate(); // 空单盈亏方向相反
                }
                unRealizedProfit = priceDiff.multiply(holdQty).setScale(4, RoundingMode.HALF_UP);
            }

            // 计算名义价值 notional = 持仓数量 * 标记价格
            BigDecimal notional = holdQty.multiply(currentPrice).setScale(4, RoundingMode.HALF_UP);

            // 计算保证金相关字段
            // initialMargin = notional / leverage
            BigDecimal initialMargin = notional.divide(BigDecimal.valueOf(leverage), 4, RoundingMode.HALF_UP);
            // maintMargin 约为 initialMargin 的 0.4% - 2%（根据币安规则，这里简化为 0.5%）
            BigDecimal maintMargin = notional.multiply(new BigDecimal("0.005")).setScale(4, RoundingMode.HALF_UP);

            // 计算盈亏平衡价（含手续费，简化处理：约等于开仓价 * 1.0004）
            BigDecimal breakEvenPrice = entryPrice.multiply(new BigDecimal("1.0004")).setScale(4, RoundingMode.HALF_UP);

            // 计算强平价格（简化计算）
            // 多头强平价 ≈ entryPrice * (1 - 1/leverage + 维持保证金率)
            // 空头强平价 ≈ entryPrice * (1 + 1/leverage - 维持保证金率)
            BigDecimal liquidationPrice;
            BigDecimal leverageRate = BigDecimal.ONE.divide(BigDecimal.valueOf(leverage), 6, RoundingMode.HALF_UP);
            BigDecimal maintRate = new BigDecimal("0.005");
            if ("LONG".equals(positionSide)) {
                liquidationPrice = entryPrice.multiply(BigDecimal.ONE.subtract(leverageRate).add(maintRate))
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                liquidationPrice = entryPrice.multiply(BigDecimal.ONE.add(leverageRate).subtract(maintRate))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // 构建模拟持仓对象
            BinancePosition position = new BinancePosition();
            position.setSymbol(session.getSymbol());
            position.setPositionSide(positionSide);
            position.setPositionAmt(holdQty);
            position.setEntryPrice(entryPrice);
            position.setBreakEvenPrice(breakEvenPrice);
            position.setMarkPrice(currentPrice);
            position.setUnRealizedProfit(unRealizedProfit);
            position.setLiquidationPrice(liquidationPrice);
            position.setIsolatedMargin(BigDecimal.ZERO); // 全仓模式下为0
            position.setNotional(notional);
            position.setMarginAsset("USDT");
            position.setIsolatedWallet(BigDecimal.ZERO); // 全仓模式下为0
            position.setInitialMargin(initialMargin);
            position.setMaintMargin(maintMargin);
            position.setPositionInitialMargin(initialMargin);
            position.setOpenOrderInitialMargin(BigDecimal.ZERO);
            position.setAdl(2); // ADL指标，1-5，2表示较低风险
            position.setBidNotional(BigDecimal.ZERO);
            position.setAskNotional(BigDecimal.ZERO);
            position.setUpdateTime(System.currentTimeMillis());

            positions.add(position);
        }

        return positions;
    }


    /**
     * 从会话的bizParam中解析持仓方向
     */
    private String parsePositionSide(AppAccountSessionDO session) {
        if (StringUtils.isBlank(session.getBizParam())) {
            return null;
        }
        try {
            StrategyBizParam bizParam = JSON.parseObject(session.getBizParam(), StrategyBizParam.class);
            return bizParam.getOpen().getPosition();
        } catch (Exception e) {
            log.warn("parsePositionSide -> 解析bizParam失败, sessionId={}, bizParam={}",
                    session.getId(), session.getBizParam(), e);
        }
        return null;
    }


    @Override
    public HistoryLineVO historyLine(AppUserDO userLogin, AccountHistoryLineReqVO reqVO) {

        long hours = Duration.between(reqVO.getStartTime(), reqVO.getEndTime()).toHours();

        HistoryLineVO lineVO = new HistoryLineVO();

        if (hours <= 8) {
            Long start = Long.parseLong(DateUtils.beauty(reqVO.getStartTime(), "yyyyMMddHHmm"));
            Long end = Long.parseLong(DateUtils.beauty(reqVO.getEndTime(), "yyyyMMddHHmm"));
            List<StatAccountEquityLogMinuteDO> list = accountEquityLogMinuteRepository.listByTimeRange(userLogin.getId(), reqVO.getAccountId(), start, end);
            if (CollectionUtils.isNotEmpty(list)) {
                for (StatAccountEquityLogMinuteDO minuteDO : list) {
                    lineVO.getLineX().add(minuteDO.getTimeLong().toString());
                    lineVO.getLineY().add(minuteDO.getEquity().setScale(2, RoundingMode.HALF_UP));
                }
            }
        } else if (hours <= 336) {
            Long start = Long.parseLong(DateUtils.beauty(reqVO.getStartTime(), "yyyyMMddHH"));
            Long end = Long.parseLong(DateUtils.beauty(reqVO.getEndTime(), "yyyyMMddHH"));
            List<StatAccountEquityLogHourDO> list = accountEquityLogHourRepository.listByTimeRange(userLogin.getId(), reqVO.getAccountId(), start, end);
            if (CollectionUtils.isNotEmpty(list)) {
                for (StatAccountEquityLogHourDO dayDO : list) {
                    lineVO.getLineX().add(dayDO.getTimeLong().toString());
                    lineVO.getLineY().add(dayDO.getEquity().setScale(2, RoundingMode.HALF_UP));
                }
            }
        } else {
            Long start = Long.parseLong(DateUtils.beauty(reqVO.getStartTime(), "yyyyMMdd"));
            Long end = Long.parseLong(DateUtils.beauty(reqVO.getEndTime(), "yyyyMMdd"));
            List<StatAccountEquityLogDayDO> list = accountEquityLogDayRepository.listByTimeRange(userLogin.getId(), reqVO.getAccountId(), start, end);
            if (CollectionUtils.isNotEmpty(list)) {
                for (StatAccountEquityLogDayDO dayDO : list) {
                    lineVO.getLineX().add(dayDO.getTimeLong().toString());
                    lineVO.getLineY().add(dayDO.getEquity().setScale(2, RoundingMode.HALF_UP));
                }
            }
        }
        return lineVO;
    }

    @Override
    public void syncEquity() {
        List<AppAccountDO> accountDOList = appAccountRepository.listNeedSync(LocalDateTime.now());
        if (CollectionUtils.isEmpty(accountDOList)) {
            return;
        }
        Semaphore semaphore = new Semaphore(5);
        CountDownLatch latch = new CountDownLatch(accountDOList.size());

        for (AppAccountDO accountDO : accountDOList) {
            semaphore.acquireUninterruptibly();
            Thread.ofVirtual().name("VT-" + accountDO.getId()).start(() -> {
                try {
                    this.sync(accountDO);
                    log.warn("syncEquity -> completed, accountId={}", accountDO.getId());
                } catch (Throwable r) {
                    log.error("UserSocketKeepaliveJob doJob error", r);
                } finally {
                    semaphore.release();
                    latch.countDown();
                }
            });
        }
    }

    @Override
    public AccountResVO accountInfo(AppUserDO userLogin, Long accountId) {
        AppAccountDO accountDO = appAccountRepository.getById(accountId);

        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());

        AccountResVO resVO = new AccountResVO(accountDO);
        AppStrategyInstanceDO ins = appStrategyInstanceRepository.getById(accountDO.getStrategyInstanceId());
        if (null != ins) {
            resVO.setStrategyCode(ins.getCode());
            resVO.setStrategyInstanceName(ins.getName());
        }
        SessionAmtBO leftAmt = appAccountSessionRepository.sumAmtByAccount(accountDO.getId(), null,
                accountDO.getLastSyncSessionId(), SessionStatusEnum.completed());
        resVO.setClosePnl(resVO.getClosePnl().add(leftAmt.getClosePnl()));
        resVO.setOpenFee(resVO.getOpenFee().add(leftAmt.getOpenFee()));
        resVO.setCloseFee(resVO.getCloseFee().add(leftAmt.getCloseFee()));

        SessionAmtBO todayAmt = appAccountSessionRepository.sumAmtByAccount(accountDO.getId(), LocalDate.now().atStartOfDay(),
                null, SessionStatusEnum.completed());
        resVO.setTodayClosePnl(todayAmt.getClosePnl());
        resVO.setTodayOpenFee(todayAmt.getOpenFee());
        resVO.setTodayCloseFee(todayAmt.getCloseFee());
        return resVO;
    }

    @Override
    public SessionMobResVO runningSession(AppUserDO userLogin, Long accountId) {
        AppAccountDO accountDO = appAccountRepository.getById(accountId);
        return runningSession(userLogin, accountDO, null);
    }

    private SessionMobResVO runningSession(AppUserDO userLogin, AppAccountDO accountDO, Long sessionId) {
        AppAccountSessionDO sessionDO;
        if (null == sessionId) {
            sessionDO = appAccountSessionRepository.getLastByAccountId(accountDO.getId());
        } else {
            sessionDO = appAccountSessionRepository.getById(sessionId);
        }
        SessionMobResVO resVO = new SessionMobResVO();
        if (null != sessionDO) {
            PermissionUtils.checkPermission(userLogin, sessionDO.getUserId());
            BeanUtils.copyProperties(sessionDO, resVO);
        }
        if (TradeTypeEnum.REAL.equalsByCode(accountDO.getTradeType())) {
            resVO.setAlgoOrders(accountDO.openAlgoOrders());
            resVO.setPositions(accountDO.positionRisk());
        } else if (null != sessionDO && SessionStatusEnum.running().contains(sessionDO.getStatus())) {
            resVO.setPositions(this.mockPosition(sessionDO));
        }
        return resVO;
    }

    @Override
    public void stopSession(AppUserDO userLogin, Long accountId, String position, Long sessionId) {
        AppAccountDO accountDO = appAccountRepository.getById(accountId);
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());
        // 获取持仓（区分模拟/实盘）
        LocalLock.executeWithoutResult("SESSION_" + sessionId, () -> {
            SessionMobResVO sessionVO = this.runningSession(userLogin, accountDO, sessionId);
            AppAccountOrderDO orderDO = null;
            List<BinancePosition> positionList = sessionVO.getPositions();

            if (CollectionUtils.isNotEmpty(positionList)) {
                for (BinancePosition ps : positionList) {
                    if (ps.getPositionSide().equals(position)) {
                        BigDecimal curPrice = klineCacheFactory.getCurPrice(SymbolEnum.valueOf(accountDO.getSymbol()), KlineIntervalEnum.MINUTE_1);
                        orderDO = appAccountOrderService.closeMarket(null, accountDO, sessionVO, position, curPrice, ps.getPositionAmt().abs());
                        log.warn("stopSession -> 手动平仓完成, accountId={}, orderDTO={}", accountId, JSON.toJSONString(orderDO));
                        positionList.remove(ps);
                        break;
                    }
                }
            }

            if (null != sessionVO.getId() && SessionStatusEnum.running().contains(sessionVO.getStatus())) {
                AppAccountSessionDO update = new AppAccountSessionDO();
                update.setId(sessionVO.getId());
                StrategyBizParam bizParam = JSON.parseObject(sessionVO.getBizParam(), StrategyBizParam.class);
                if (null != orderDO) {
                    if (position.equals(bizParam.getOpen().getPosition())) {
                        update.setTakeProfitClientOrderId(orderDO.getClientOrderId());
                        update.setTakeProfitOrderId(orderDO.getOrderId());
                    } else {
                        update.setStopLossClientOrderId(orderDO.getClientOrderId());
                        update.setStopLossOrderId(orderDO.getOrderId());
                    }
                }
                if (positionList.isEmpty()) {
                    log.warn("stopSession -> stop session, riskList is empty, change session status, sessionId={}, opPosition={}", sessionVO.getId(), position);
                    update.setStatus(SessionStatusEnum.STOP_HAND.code());
                }
                appAccountSessionRepository.updateById(update);
            }
        });
    }


    @Override
    public void sync(Long accountId) {
        AppAccountDO accountDO = appAccountRepository.getById(accountId);
        this.sync(accountDO);
    }

    @Override
    public void open(AppUserDO userLogin, Long accountId, String position, BigDecimal amount, Long sessionId) {
        AppAccountDO accountDO = appAccountRepository.getById(accountId);
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());
        LocalLock.executeWithoutResult("SESSION_" + sessionId, () -> {
            AppAccountSessionDO sessionDO;
            if (null == sessionId) {
                sessionDO = appAccountSessionRepository.getLastByAccountId(accountId);
            } else {
                sessionDO = appAccountSessionRepository.getById(sessionId);
            }

            BigDecimal curPrice = klineCacheFactory.getCurPrice(SymbolEnum.valueOf(accountDO.getSymbol()), KlineIntervalEnum.MINUTE_1);

            AppAccountOrderDO orderDO = appAccountOrderService.openMarket(null, accountDO, sessionDO == null ? -1L : sessionDO.getId(), position, curPrice, amount);

            if (sessionDO != null && SessionStatusEnum.running().contains(sessionDO.getStatus())) {
                AppAccountSessionDO update = new AppAccountSessionDO();
                update.setId(sessionDO.getId());
                // 更新对应会话的持仓价格、持仓数量
                BigDecimal openAmount = sessionDO.getHoldAvePrice().multiply(sessionDO.getHoldQty());
                BigDecimal appendAmount = orderDO.getAvePrice().multiply(orderDO.getQty());
                BigDecimal avePrice = openAmount.add(appendAmount).divide(sessionDO.getHoldQty().add(orderDO.getQty()), 6, RoundingMode.HALF_UP);
                update.setHoldAvePrice(avePrice);
                update.setHoldQty(sessionDO.getHoldQty().add(orderDO.getQty()));
                appAccountSessionRepository.updateById(update);
            }
        });

    }

    @Override
    public void algoOrder(AppUserDO userLogin, AlgoOrderReqVO reqVO) {
        AppAccountDO accountDO = appAccountRepository.getById(reqVO.getAccountId());

        List<BinancePosition> positionList = accountDO.positionRisk();
        if (CollectionUtils.isEmpty(positionList)) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.HAS_NO_POSITION);
        }

        BinancePosition desP = null;

        for (BinancePosition p : positionList) {
            if (p.getPositionSide().equalsIgnoreCase(reqVO.getPositionSide())) {
                desP = p;
            }
        }

        if (desP == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.HAS_NO_POSITION);
        }

        boolean longPosition = "LONG".equals(reqVO.getPositionSide());
        Long takeProfitAlgoId = -1L;
        Long stopLossAlgoId = -1L;

        List<BinanceAlgoOrderDTO> algoOrders = accountDO.openAlgoOrders();
        if (CollectionUtils.isNotEmpty(algoOrders)) {
            for (BinanceAlgoOrderDTO algoOrder : algoOrders) {
                if (algoOrder.getPositionSide().equalsIgnoreCase(reqVO.getPositionSide())) {
                    accountDO.cancelAlgoOrders(algoOrder.getClientAlgoId(), algoOrder.getAlgoId());
                    log.warn("algoOrder -> 取消条件单, accountId={}, algoId:{}, position={}, type={}, price={}",
                            reqVO.getAccountId(), algoOrder.getAlgoId(), algoOrder.getPositionSide(),
                            algoOrder.getOrderType(), algoOrder.getPrice());
                }
            }
        }

        if (null != reqVO.getTakeProfitPrice()) {
            this.checkAlgoPrice(accountDO, reqVO.getTakeProfitPrice(), longPosition, true);
            BinanceAlgoOrderDTO orderDTO = accountDO.takeProfit(longPosition, reqVO.getTakeProfitPrice());
            takeProfitAlgoId = orderDTO.getAlgoId();
        }

        if (null != reqVO.getStopLossPrice()) {
            this.checkAlgoPrice(accountDO, reqVO.getStopLossPrice(), longPosition, false);
            BinanceAlgoOrderDTO orderDTO = accountDO.stopLoss(longPosition, reqVO.getStopLossPrice());
            stopLossAlgoId = orderDTO.getAlgoId();
        }

        AppAccountSessionDO sessionDO = null == reqVO.getSessionId() ? null : appAccountSessionRepository.getById(reqVO.getSessionId());

        if (null != sessionDO) {
            AppAccountSessionDO update = new AppAccountSessionDO();
            update.setId(sessionDO.getId());
            update.setTakeProfitAlgoId(takeProfitAlgoId);
            update.setStopLossAlgoId(stopLossAlgoId);
            appAccountSessionRepository.updateById(update);
        }
    }

    private void checkAlgoPrice(AppAccountDO accountDO, BigDecimal price, boolean isLong, Boolean takeProfit) {
        BigDecimal curPrice = klineCacheFactory.getCurPrice(SymbolEnum.valueOf(accountDO.getSymbol()), KlineIntervalEnum.MINUTE_1);
        if (takeProfit) {
            if (isLong) {
                if (price.compareTo(curPrice) <= 0) {
                    throw new APIRuntimeException(IResponseStatusMsg.APIEnum.TAKE_PROFIT_PRICE_MUST_GATHER_CUR);
                }
            } else {
                if (price.compareTo(curPrice) >= 0) {
                    throw new APIRuntimeException(IResponseStatusMsg.APIEnum.TAKE_PROFIT_PRICE_MUST_LESS_CUR);
                }
            }
        } else {
            if (isLong) {
                if (price.compareTo(curPrice) >= 0) {
                    throw new APIRuntimeException(IResponseStatusMsg.APIEnum.STOP_LOSS_PRICE_MUST_LESS_CUR);
                }
            } else {
                if (price.compareTo(curPrice) <= 0) {
                    throw new APIRuntimeException(IResponseStatusMsg.APIEnum.STOP_LOSS_PRICE_MUST_GATHER_CUR);
                }
            }
        }
    }

    private void sync(AppAccountDO accountDO) {
        BigDecimal equity;

        if (TradeTypeEnum.MOCK.equalsByCode(accountDO.getTradeType())) {
            // 模拟模式：基于初始净值 + 已平仓盈亏 - 手续费计算
            equity = accountDO.getInitEquity() != null ? accountDO.getInitEquity() : BigDecimal.ZERO;
            BigDecimal closePnl = accountDO.getClosePnl() != null ? accountDO.getClosePnl() : BigDecimal.ZERO;
            BigDecimal openFee = accountDO.getOpenFee() != null ? accountDO.getOpenFee() : BigDecimal.ZERO;
            BigDecimal closeFee = accountDO.getCloseFee() != null ? accountDO.getCloseFee() : BigDecimal.ZERO;
            equity = equity.add(closePnl).subtract(openFee).subtract(closeFee);
            log.debug("sync -> 模拟模式，使用本地净值计算, accountId={}, equity={}", accountDO.getId(), equity);
        } else {
            // 实盘模式：调用币安API查询余额
            List<FuturesAccountAssetDTO> assetDTOList = accountDO.fetchBalanceV3();
            equity = BigDecimal.ZERO;
            if (CollectionUtils.isNotEmpty(assetDTOList)) {
                for (FuturesAccountAssetDTO assetDTO : assetDTOList) {
                    if (assetDTO.getAsset().contains("USD")) {
                        equity = equity.add(assetDTO.getBalance());
                    }
                }
            }
        }

        // 写入净值日志
        AccountEquityAddBO addBO = new AccountEquityAddBO();
        addBO.setAccountId(accountDO.getId());
        addBO.setEquity(equity);
        addBO.setTime(LocalDateTime.now());
        accountEquityService.addLog(accountDO, addBO);

        // 更新账户净值
        AppAccountDO update = new AppAccountDO();
        update.setId(addBO.getAccountId());
        update.setCurEquity(addBO.getEquity());
        update.setNextSyncTime(LocalDateTime.now().plusSeconds(new SecureRandom().nextInt(25, 59)));
        update.setUpdateTime(LocalDateTime.now());

        // 同步金额数据
        SessionAmtBO amtBO = appAccountSessionRepository.sumAmtByAccount(accountDO.getId(), null,
                accountDO.getLastSyncSessionId(), SessionStatusEnum.completed());
        if (null != amtBO.getMaxId()) {
            update.setLastSyncSessionId(amtBO.getMaxId());
        }

        // 空值安全处理
        BigDecimal oldClosePnl = accountDO.getClosePnl() != null ? accountDO.getClosePnl() : BigDecimal.ZERO;
        BigDecimal oldOpenFee = accountDO.getOpenFee() != null ? accountDO.getOpenFee() : BigDecimal.ZERO;
        BigDecimal oldCloseFee = accountDO.getCloseFee() != null ? accountDO.getCloseFee() : BigDecimal.ZERO;
        BigDecimal addClosePnl = amtBO.getClosePnl() != null ? amtBO.getClosePnl() : BigDecimal.ZERO;
        BigDecimal addOpenFee = amtBO.getOpenFee() != null ? amtBO.getOpenFee() : BigDecimal.ZERO;
        BigDecimal addCloseFee = amtBO.getCloseFee() != null ? amtBO.getCloseFee() : BigDecimal.ZERO;

        update.setClosePnl(oldClosePnl.add(addClosePnl));
        update.setOpenFee(oldOpenFee.add(addOpenFee));
        update.setCloseFee(oldCloseFee.add(addCloseFee));

        appAccountRepository.updateById(update);
    }


    /**
     * 构建模拟持仓数据
     *
     * @param sessionDO 会话信息
     * @return 模拟持仓列表
     */
    private List<BinancePosition> mockPosition(AppAccountSessionDO sessionDO) {
        List<BinancePosition> positionList = new ArrayList<>();
        StrategyBizParam bizParam = JSON.parseObject(sessionDO.getBizParam(), StrategyBizParam.class);
        BigDecimal curPrice = klineCacheFactory.getCurPrice(SymbolEnum.valueOf(sessionDO.getSymbol()), KlineIntervalEnum.MINUTE_1);

        BigDecimal openPnl = curPrice.subtract(sessionDO.getHoldAvePrice()).multiply(sessionDO.getHoldQty());
        // 构建主持仓
        BinancePosition mainPosition = new BinancePosition();
        mainPosition.setSymbol(sessionDO.getSymbol());
        mainPosition.setPositionSide(bizParam.getOpen().getPosition());
        mainPosition.setPositionAmt(sessionDO.getHoldQty());
        mainPosition.setEntryPrice(sessionDO.getHoldAvePrice());
        mainPosition.setUnRealizedProfit("LONG".equals(bizParam.getOpen().getPosition()) ? openPnl : openPnl.negate());
        positionList.add(mainPosition);

        // 如果有对冲持仓
        StrategyBizOrderParam reverse = bizParam.getReverse();
        if (SessionBizStatusEnum.HOLD_ALL.equalsByCode(sessionDO.getBizStatus()) && null != reverse && null != reverse.getOrderId()) {
            BigDecimal reversePnl = curPrice.subtract(bizParam.getReverse().getPrice()).multiply(bizParam.getReverse().getQty());
            BinancePosition reversePos = new BinancePosition();
            reversePos.setSymbol(sessionDO.getSymbol());
            reversePos.setPositionSide(reverse.getPosition());
            reversePos.setPositionAmt(reverse.getQty());
            reversePos.setEntryPrice(reverse.getPrice());
            reversePos.setUnRealizedProfit("LONG".equals(reverse.getPosition()) ? reversePnl : reversePnl.negate());
            positionList.add(reversePos);
        }
        return positionList;
    }

    @Override
    public SessionDetailVO getCurrentSession(AppUserDO userLogin, Long accountId) {
        AppAccountDO accountDO = appAccountRepository.getById(accountId);
        if (accountDO == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_NOT_EXIST);
        }

        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());

        // 获取最近的会话
        AppAccountSessionDO sessionDO = appAccountSessionRepository.getLastByAccountId(accountId);
        if (sessionDO == null) {
            return null;
        }

        // 只返回运行中的会话详情
        if (!SessionStatusEnum.running().contains(sessionDO.getStatus())) {
            return null;
        }

        SessionDetailVO detailVO = new SessionDetailVO(sessionDO);

        // 获取当前持仓（根据交易类型区分）
        List<BinancePosition> positions;
        if (TradeTypeEnum.REAL.equalsByCode(accountDO.getTradeType())) {
            positions = accountDO.positionRisk();
        } else {
            positions = this.mockPosition(sessionDO);
        }
        detailVO.setPositions(positions);

        // 从持仓中提取持仓方向
        if (CollectionUtils.isNotEmpty(positions)) {
            for (BinancePosition pos : positions) {
                if (pos.getPositionAmt() != null && pos.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
                    detailVO.setPositionSide(pos.getPositionSide());
                    break;
                }
            }
        }

        // 获取当前价格并计算未实现盈亏
        try {
            BigDecimal curPrice = klineCacheFactory.getCurPrice(
                    SymbolEnum.valueOf(accountDO.getSymbol()),
                    KlineIntervalEnum.MINUTE_1
            );
            detailVO.setCurrentPrice(curPrice);

            // 计算持仓价值
            if (detailVO.getHoldQty() != null && detailVO.getHoldAvePrice() != null) {
                detailVO.setHoldValue(detailVO.getHoldAvePrice().multiply(detailVO.getHoldQty()));

                // 计算未实现盈亏
                if (curPrice != null && detailVO.getHoldQty().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal priceDiff = curPrice.subtract(detailVO.getHoldAvePrice());
                    // 根据持仓方向计算盈亏
                    if ("SHORT".equals(detailVO.getPositionSide())) {
                        priceDiff = priceDiff.negate(); // 空单：开仓价 - 当前价
                    }
                    BigDecimal unrealizedPnl = priceDiff.multiply(detailVO.getHoldQty());
                    detailVO.setUnrealizedPnl(unrealizedPnl);

                    // 计算盈亏百分比
                    if (detailVO.getHoldValue().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal pnlPercent = unrealizedPnl
                                .divide(detailVO.getHoldValue(), 6, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"));
                        detailVO.setUnrealizedPnlPercent(pnlPercent);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("getCurrentSession -> 获取当前价格失败, accountId={}, error={}", accountId, e.getMessage());
        }

        return detailVO;
    }

    @Override
    public PageResponseVO<SessionHistoryVO> getSessionHistory(AppUserDO userLogin, PageRequestVO<SessionHistoryReqVO> requestVO) {
        SessionHistoryReqVO param = requestVO.getParam();
        if (param == null || param.getAccountId() == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR, "accountId不能为空");
        }

        // 验证账号权限
        AppAccountDO accountDO = appAccountRepository.getById(param.getAccountId());
        if (accountDO == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_NOT_EXIST);
        }
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());

        // 分页查询
        IPage<AppAccountSessionDO> page = appAccountSessionRepository.pageList(
                param.getAccountId(),
                param.getStatus(),
                param.getStartTime(),
                param.getEndTime(),
                param.getSymbol(),
                requestVO.getPage(),
                requestVO.getPageSize()
        );

        // 转换结果
        PageResponseVO<SessionHistoryVO> response = new PageResponseVO<>();
        response.setTotal(page.getTotal());
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            response.setRecords(page.getRecords().stream()
                    .map(SessionHistoryVO::new)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    @Override
    public PageResponseVO<OrderHistoryVO> getOrderHistory(AppUserDO userLogin, PageRequestVO<OrderHistoryReqVO> requestVO) {
        OrderHistoryReqVO param = requestVO.getParam();
        if (param == null || param.getAccountId() == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.PARAM_ERROR, "accountId不能为空");
        }

        // 验证账号权限
        AppAccountDO accountDO = appAccountRepository.getById(param.getAccountId());
        if (accountDO == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_NOT_EXIST);
        }
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());

        // 分页查询
        IPage<AppAccountOrderDO> page = appAccountOrderRepository.pageList(
                param.getAccountId(),
                param.getSessionId(),
                param.getOrderType(),
                param.getStatus(),
                param.getPositionSide(),
                param.getSymbol(),
                param.getStartTime(),
                param.getEndTime(),
                requestVO.getPage(),
                requestVO.getPageSize()
        );

        // 转换结果
        PageResponseVO<OrderHistoryVO> response = new PageResponseVO<>();
        response.setTotal(page.getTotal());
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            response.setRecords(page.getRecords().stream()
                    .map(OrderHistoryVO::new)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    @Override
    public StrategyStatusVO getStrategyStatus(AppUserDO userLogin, Long accountId) {
        // 验证账号
        AppAccountDO accountDO = appAccountRepository.getById(accountId);
        if (accountDO == null) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_NOT_EXIST);
        }
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());

        StrategyStatusVO statusVO = new StrategyStatusVO();

        // 基础信息
        statusVO.setAccountId(accountDO.getId());
        statusVO.setAccountName(accountDO.getName());
        statusVO.setStrategyInstanceId(accountDO.getStrategyInstanceId());
        statusVO.setSymbol(accountDO.getSymbol());
        statusVO.setStrategyStatus(accountDO.getStrategyStatus());
        statusVO.setStrategyStatusName(StrategyStatusEnum.RUNNING.code().equals(accountDO.getStrategyStatus()) ? "运行中" : "已停止");

        // 获取策略实例信息
        if (accountDO.getStrategyInstanceId() != null) {
            AppStrategyInstanceDO instanceDO = appStrategyInstanceRepository.getById(accountDO.getStrategyInstanceId());
            if (instanceDO != null) {
                statusVO.setStrategyName(instanceDO.getName());
                statusVO.setStrategyCode(instanceDO.getCode());
            }
        }

        // 已完成状态列表
        List<Integer> completedStatusList = SessionStatusEnum.completed();

        // 累计统计
        StrategyStatBO totalStat = appAccountSessionRepository.statByAccount(accountId, null, completedStatusList);
        statusVO.setTotalTradeCount(totalStat.getTotalCount() != null ? totalStat.getTotalCount() : 0);
        statusVO.setTotalWinCount(totalStat.getWinCount() != null ? totalStat.getWinCount() : 0);
        statusVO.setTotalLoseCount(totalStat.getLoseCount() != null ? totalStat.getLoseCount() : 0);
        statusVO.setTotalPnl(totalStat.getTotalPnl() != null ? totalStat.getTotalPnl() : BigDecimal.ZERO);
        statusVO.setTotalProfit(totalStat.getTotalProfit() != null ? totalStat.getTotalProfit() : BigDecimal.ZERO);
        statusVO.setTotalLoss(totalStat.getTotalLoss() != null ? totalStat.getTotalLoss() : BigDecimal.ZERO);
        statusVO.setMaxProfit(totalStat.getMaxProfit() != null ? totalStat.getMaxProfit() : BigDecimal.ZERO);
        statusVO.setMaxLoss(totalStat.getMaxLoss() != null ? totalStat.getMaxLoss() : BigDecimal.ZERO);

        BigDecimal totalOpenFee = totalStat.getTotalOpenFee() != null ? totalStat.getTotalOpenFee() : BigDecimal.ZERO;
        BigDecimal totalCloseFee = totalStat.getTotalCloseFee() != null ? totalStat.getTotalCloseFee() : BigDecimal.ZERO;
        statusVO.setTotalFee(totalOpenFee.add(totalCloseFee));

        // 今日统计
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        StrategyStatBO todayStat = appAccountSessionRepository.statByAccount(accountId, todayStart, completedStatusList);
        statusVO.setTodayTradeCount(todayStat.getTotalCount() != null ? todayStat.getTotalCount() : 0);
        statusVO.setTodayWinCount(todayStat.getWinCount() != null ? todayStat.getWinCount() : 0);
        statusVO.setTodayLoseCount(todayStat.getLoseCount() != null ? todayStat.getLoseCount() : 0);
        statusVO.setTodayPnl(todayStat.getTotalPnl() != null ? todayStat.getTotalPnl() : BigDecimal.ZERO);

        BigDecimal todayOpenFee = todayStat.getTotalOpenFee() != null ? todayStat.getTotalOpenFee() : BigDecimal.ZERO;
        BigDecimal todayCloseFee = todayStat.getTotalCloseFee() != null ? todayStat.getTotalCloseFee() : BigDecimal.ZERO;
        statusVO.setTodayFee(todayOpenFee.add(todayCloseFee));

        // 计算衍生指标
        statusVO.calculate();

        return statusVO;
    }

    @Override
    public List<AppStrategyInstanceDO> strategyList(AppUserDO userLogin, Long accountId) {
        return appStrategyInstanceRepository.listByStatus(StrategyInstanceStatusEnum.ONLINE.getCode());
    }

    @Override
    public void updateStrategy(AppUserDO userLogin, AccountStrategyUpdateReqVO reqVO) {
        AppAccountDO accountDO = appAccountRepository.getById(reqVO.getId());
        if (null == accountDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_ID_NOT_EXIST);
        }
        if (StrategyStatusEnum.RUNNING.equalsByCode(accountDO.getStrategyStatus())) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.STRATEGY_IS_RUNNING);
        }

        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());

        AppAccountDO update = new AppAccountDO();
        update.setId(accountDO.getId());

        if (null != reqVO.getStrategyInstanceId()) {
            AppStrategyInstanceDO instanceDO = appStrategyInstanceRepository.getById(reqVO.getStrategyInstanceId());
            if (null == instanceDO) {
                throw new APIRuntimeException(IResponseStatusMsg.APIEnum.INVALID_STRATEGY_INSTANCE_ID);
            }
            update.setStrategyInstanceId(reqVO.getStrategyInstanceId());
            update.setSymbol(instanceDO.getSymbol());
        }

        if (null != reqVO.getLeverage()) {
            update.setLeverage(reqVO.getLeverage());
            if (TradeTypeEnum.REAL.equalsByCode(accountDO.getTradeType())) {
                String leverRes = accountDO.changeLeverage(reqVO.getLeverage());
                log.warn("updateStrategy -> 修改当前用户杠杆比例，leverage={}, resp={}", reqVO.getLeverage(), leverRes);
            }
        }
        appAccountRepository.updateById(update);
    }

    @Override
    public List<PositionDetailVO> positionRiskDetail(AppUserDO userLogin, Long accountId) {
        AppAccountDO accountDO = appAccountRepository.getById(accountId);
        if (null == accountDO) {
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.ACCOUNT_ID_NOT_EXIST);
        }
        PermissionUtils.checkPermission(userLogin, accountDO.getUserId());

        List<PositionDetailVO> result = new ArrayList<>();

        // 查询运行中和异常状态的会话（反仓后的对冲仓位也需要显示）
        List<Integer> queryStatusList = new ArrayList<>(SessionStatusEnum.running());
        List<AppAccountSessionDO> runningSessions = appAccountSessionRepository.listByAccountIdAndStatusList(accountDO.getId(), queryStatusList);
        if (CollectionUtils.isEmpty(runningSessions)) {
            return result;
        }

        SymbolEnum symbolEnum = SymbolEnum.valueOf(accountDO.getSymbol());
        BigDecimal currentPrice = klineCacheFactory.getCurPrice(symbolEnum, KlineIntervalEnum.MINUTE_1);
        if (currentPrice == null) {
            currentPrice = BigDecimal.ZERO;
        }

        int leverage = accountDO.getLeverage() != null ? accountDO.getLeverage() : 20;

        List<BinancePosition> realPositions = null;
        if (!TradeTypeEnum.MOCK.equalsByCode(accountDO.getTradeType())) {
            try {
                realPositions = accountDO.positionRisk();
            } catch (Exception e) {
                log.warn("positionRiskDetail -> 获取真实持仓失败, accountId={}", accountId, e);
            }
        }

        for (AppAccountSessionDO session : runningSessions) {
            if (session.getHoldQty() == null || session.getHoldQty().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            // 构建原仓位
            PositionDetailVO vo = buildPositionDetailVO(session, accountDO, currentPrice, leverage, realPositions);
            if (vo != null) {
                result.add(vo);
            }

            // 检查是否已反仓，如果已反仓则构建反向仓位
            if (StringUtils.isNotBlank(session.getBizParam())) {
                try {
                    JSONObject bizJson = JSON.parseObject(session.getBizParam());
                    BigDecimal aveReversePrice = bizJson.getBigDecimal("aveReversePrice");
                    BigDecimal reverseQty = bizJson.getBigDecimal("reverseQty");
                    String openPosition = bizJson.getString("openPosition");

                    if (aveReversePrice != null && reverseQty != null && reverseQty.compareTo(BigDecimal.ZERO) > 0) {
                        // 构建反向仓位
                        PositionDetailVO reverseVo = buildReversePositionVO(session, accountDO, currentPrice, leverage,
                                realPositions, openPosition, aveReversePrice, reverseQty);
                        if (reverseVo != null) {
                            result.add(reverseVo);
                        }
                    }
                } catch (Exception e) {
                    log.warn("positionRiskDetail -> 解析反仓信息失败, sessionId={}", session.getId(), e);
                }
            }
        }

        return result;
    }

    private PositionDetailVO buildPositionDetailVO(AppAccountSessionDO session, AppAccountDO accountDO,
                                                   BigDecimal currentPrice, int leverage,
                                                   List<BinancePosition> realPositions) {
        PositionDetailVO vo = new PositionDetailVO();

        vo.setSessionId(session.getId());
        vo.setSymbol(session.getSymbol());
        vo.setPositionAmt(session.getHoldQty());
        vo.setEntryPrice(session.getHoldAvePrice());
        vo.setMarkPrice(currentPrice);
        vo.setLeverage(leverage);
        vo.setStrategyCode(session.getStrategyCode());
        vo.setMockData(session.getMockData() != null && session.getMockData() == 1);
        vo.setTakeProfitPrice(session.getTakeProfitPrice());
        vo.setStopLossPrice(session.getStopLossPrice());

        String positionSide = null;
        int appendCount = 0;
        int maxAppendCount = 2;
        BigDecimal nextAppendPrice = null;
        BigDecimal reversePrice = null;
        boolean reversed = false;

        if (StringUtils.isNotBlank(session.getBizParam())) {
            try {
                JSONObject bizJson = JSON.parseObject(session.getBizParam());
                positionSide = bizJson.getString("openPosition");
                if (positionSide == null) {
                    positionSide = bizJson.getString("positionSide");
                }

                if ("MARTINGALE".equals(session.getStrategyCode())
                        || "MARTINGALE_SIGNAL".equals(session.getStrategyCode())
                        || "MARTINGALE_APPEND".equals(session.getStrategyCode())) {
                    if (bizJson.getBigDecimal("aveAppendPrice1") != null) appendCount = 1;
                    if (bizJson.getBigDecimal("aveAppendPrice2") != null) appendCount = 2;
                    if (appendCount == 0) nextAppendPrice = bizJson.getBigDecimal("appendPrice1");
                    else if (appendCount == 1) nextAppendPrice = bizJson.getBigDecimal("appendPrice2");
                    reversePrice = bizJson.getBigDecimal("reversePrice");
                    reversed = bizJson.getBigDecimal("aveReversePrice") != null;
                } else {
                    maxAppendCount = 1;
                    if (bizJson.getBigDecimal("aveAppendPrice") != null) appendCount = 1;
                    if (appendCount == 0) nextAppendPrice = bizJson.getBigDecimal("appendPrice");
                    reversePrice = bizJson.getBigDecimal("reversePrice");
                    reversed = bizJson.getBigDecimal("aveReversePrice") != null;
                }
            } catch (Exception e) {
                log.warn("buildPositionDetailVO -> 解析bizParam失败, sessionId={}", session.getId(), e);
            }
        }

        vo.setPositionSide(positionSide);
        vo.setAppendCount(appendCount);
        vo.setMaxAppendCount(maxAppendCount);
        vo.setNextAppendPrice(nextAppendPrice);
        vo.setReversePrice(reversePrice);
        vo.setReversed(reversed);

        BigDecimal entryPrice = session.getHoldAvePrice() != null ? session.getHoldAvePrice() : BigDecimal.ZERO;
        BigDecimal holdQty = session.getHoldQty();

        if (currentPrice.compareTo(BigDecimal.ZERO) > 0 && entryPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal priceDiff = currentPrice.subtract(entryPrice);
            if ("SHORT".equals(positionSide)) priceDiff = priceDiff.negate();
            BigDecimal unRealizedProfit = priceDiff.multiply(holdQty).setScale(4, RoundingMode.HALF_UP);
            vo.setUnRealizedProfit(unRealizedProfit);

            BigDecimal costValue = entryPrice.multiply(holdQty);
            if (costValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percent = unRealizedProfit.divide(costValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                vo.setUnRealizedProfitPercent(percent);
            }
        }

        BigDecimal notional = holdQty.multiply(currentPrice).setScale(4, RoundingMode.HALF_UP);
        vo.setNotional(notional);

        BigDecimal initialMargin = notional.divide(BigDecimal.valueOf(leverage), 4, RoundingMode.HALF_UP);
        BigDecimal maintMargin = notional.multiply(new BigDecimal("0.005")).setScale(4, RoundingMode.HALF_UP);
        vo.setInitialMargin(initialMargin);
        vo.setMaintMargin(maintMargin);
        vo.setMarginAsset("USDT");

        BigDecimal breakEvenPrice = entryPrice.multiply(new BigDecimal("1.0004")).setScale(4, RoundingMode.HALF_UP);
        vo.setBreakEvenPrice(breakEvenPrice);

        BigDecimal leverageRate = BigDecimal.ONE.divide(BigDecimal.valueOf(leverage), 6, RoundingMode.HALF_UP);
        BigDecimal maintRate = new BigDecimal("0.005");
        BigDecimal liquidationPrice;
        if ("LONG".equals(positionSide)) {
            liquidationPrice = entryPrice.multiply(BigDecimal.ONE.subtract(leverageRate).add(maintRate)).setScale(2, RoundingMode.HALF_UP);
        } else {
            liquidationPrice = entryPrice.multiply(BigDecimal.ONE.add(leverageRate).subtract(maintRate)).setScale(2, RoundingMode.HALF_UP);
        }
        vo.setLiquidationPrice(liquidationPrice);
        vo.setUpdateTime(System.currentTimeMillis());

        if (realPositions != null && !realPositions.isEmpty()) {
            for (BinancePosition realPos : realPositions) {
                if (realPos.getPositionSide() != null && realPos.getPositionSide().equals(positionSide)) {
                    if (realPos.getMarkPrice() != null) vo.setMarkPrice(realPos.getMarkPrice());
                    if (realPos.getUnRealizedProfit() != null) vo.setUnRealizedProfit(realPos.getUnRealizedProfit());
                    if (realPos.getLiquidationPrice() != null) vo.setLiquidationPrice(realPos.getLiquidationPrice());
                    if (realPos.getBreakEvenPrice() != null) vo.setBreakEvenPrice(realPos.getBreakEvenPrice());
                    if (realPos.getNotional() != null) vo.setNotional(realPos.getNotional());
                    if (realPos.getInitialMargin() != null) vo.setInitialMargin(realPos.getInitialMargin());
                    if (realPos.getMaintMargin() != null) vo.setMaintMargin(realPos.getMaintMargin());
                    break;
                }
            }
        }

        return vo;
    }

    /**
     * 构建反向仓位的持仓详情（反仓后的对冲仓位）
     */
    private PositionDetailVO buildReversePositionVO(AppAccountSessionDO session, AppAccountDO accountDO,
                                                    BigDecimal currentPrice, int leverage,
                                                    List<BinancePosition> realPositions,
                                                    String openPosition, BigDecimal aveReversePrice, BigDecimal reverseQty) {
        PositionDetailVO vo = new PositionDetailVO();

        String reversePosition = "LONG".equals(openPosition) ? "SHORT" : "LONG";

        vo.setSessionId(session.getId());
        vo.setSymbol(session.getSymbol());
        vo.setPositionAmt(reverseQty);
        vo.setEntryPrice(aveReversePrice);
        vo.setMarkPrice(currentPrice);
        vo.setLeverage(leverage);
        vo.setStrategyCode(session.getStrategyCode());
        vo.setMockData(session.getMockData() != null && session.getMockData() == 1);
        vo.setPositionSide(reversePosition);
        vo.setReversed(true);

        // 反向仓位不再有加仓/反仓操作
        vo.setAppendCount(0);
        vo.setMaxAppendCount(0);
        vo.setNextAppendPrice(null);
        vo.setReversePrice(null);

        // 计算未实现盈亏
        if (currentPrice.compareTo(BigDecimal.ZERO) > 0 && aveReversePrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal priceDiff = currentPrice.subtract(aveReversePrice);
            if ("SHORT".equals(reversePosition)) priceDiff = priceDiff.negate();
            BigDecimal unRealizedProfit = priceDiff.multiply(reverseQty).setScale(4, RoundingMode.HALF_UP);
            vo.setUnRealizedProfit(unRealizedProfit);

            BigDecimal costValue = aveReversePrice.multiply(reverseQty);
            if (costValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percent = unRealizedProfit.divide(costValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                vo.setUnRealizedProfitPercent(percent);
            }
        }

        BigDecimal notional = reverseQty.multiply(currentPrice).setScale(4, RoundingMode.HALF_UP);
        vo.setNotional(notional);

        BigDecimal initialMargin = notional.divide(BigDecimal.valueOf(leverage), 4, RoundingMode.HALF_UP);
        BigDecimal maintMargin = notional.multiply(new BigDecimal("0.005")).setScale(4, RoundingMode.HALF_UP);
        vo.setInitialMargin(initialMargin);
        vo.setMaintMargin(maintMargin);
        vo.setMarginAsset("USDT");

        BigDecimal breakEvenPrice = aveReversePrice.multiply(new BigDecimal("1.0004")).setScale(4, RoundingMode.HALF_UP);
        vo.setBreakEvenPrice(breakEvenPrice);

        BigDecimal leverageRate = BigDecimal.ONE.divide(BigDecimal.valueOf(leverage), 6, RoundingMode.HALF_UP);
        BigDecimal maintRate = new BigDecimal("0.005");
        BigDecimal liquidationPrice;
        if ("LONG".equals(reversePosition)) {
            liquidationPrice = aveReversePrice.multiply(BigDecimal.ONE.subtract(leverageRate).add(maintRate)).setScale(2, RoundingMode.HALF_UP);
        } else {
            liquidationPrice = aveReversePrice.multiply(BigDecimal.ONE.add(leverageRate).subtract(maintRate)).setScale(2, RoundingMode.HALF_UP);
        }
        vo.setLiquidationPrice(liquidationPrice);
        vo.setUpdateTime(System.currentTimeMillis());

        // 如果有真实持仓数据，使用真实数据覆盖
        if (realPositions != null && !realPositions.isEmpty()) {
            for (BinancePosition realPos : realPositions) {
                if (realPos.getPositionSide() != null && realPos.getPositionSide().equals(reversePosition)) {
                    if (realPos.getMarkPrice() != null) vo.setMarkPrice(realPos.getMarkPrice());
                    if (realPos.getUnRealizedProfit() != null) vo.setUnRealizedProfit(realPos.getUnRealizedProfit());
                    if (realPos.getLiquidationPrice() != null) vo.setLiquidationPrice(realPos.getLiquidationPrice());
                    if (realPos.getBreakEvenPrice() != null) vo.setBreakEvenPrice(realPos.getBreakEvenPrice());
                    if (realPos.getNotional() != null) vo.setNotional(realPos.getNotional());
                    if (realPos.getInitialMargin() != null) vo.setInitialMargin(realPos.getInitialMargin());
                    if (realPos.getMaintMargin() != null) vo.setMaintMargin(realPos.getMaintMargin());
                    break;
                }
            }
        }

        return vo;
    }

}
