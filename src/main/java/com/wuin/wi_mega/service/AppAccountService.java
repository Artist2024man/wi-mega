package com.wuin.wi_mega.service;

import com.wuin.wi_mega.binance.bo.BinancePosition;
import com.wuin.wi_mega.binance.bo.FuturesAccountAssetDTO;
import com.wuin.wi_mega.common.enums.StrategyStatusEnum;
import com.wuin.wi_mega.model.PageRequestVO;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.vo.*;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppStrategyInstanceDO;
import com.wuin.wi_mega.repository.domain.AppUserDO;

import java.math.BigDecimal;
import java.util.List;

public interface AppAccountService {
    List<AppAccountDO> listByStatus(StrategyStatusEnum strategyStatus);

    Long create(AppUserDO userLogin, AccountCreateReqVO reqVO);

    void update(AppUserDO userLogin, AccountUpdateReqVO reqVO);

    void start(AppUserDO userLogin, AccountReqVO reqVO);

    void stop(AppUserDO userLogin, AccountReqVO reqVO);

    void delete(AppUserDO userLogin, Long id);

    PageResponseVO<AccountResVO> pageList(AppUserDO userLogin, PageRequestVO<?> requestVO);

    List<BinancePosition> positionRisk(AppUserDO userLogin, Long accountId);

    HistoryLineVO historyLine(AppUserDO userLogin, AccountHistoryLineReqVO reqVO);

    void syncEquity();

    AccountResVO accountInfo(AppUserDO userLogin, Long accountId);

    SessionMobResVO runningSession(AppUserDO userLogin, Long accountId);

    void stopSession(AppUserDO userLogin, Long accountId, String position, Long sessionId);

    void sync(Long accountId);

    void open(AppUserDO userLogin, Long accountId, String position, BigDecimal amount, Long sessionId);

    void algoOrder(AppUserDO userLogin, AlgoOrderReqVO reqVO);


    /**
     * 获取当前运行会话详情
     * @param userLogin 当前登录用户
     * @param accountId 账号ID
     * @return 会话详情，如果没有运行中的会话则返回null
     */
    SessionDetailVO getCurrentSession(AppUserDO userLogin, Long accountId);

    /**
     * 分页查询会话历史
     * @param userLogin 当前登录用户
     * @param requestVO 分页请求参数
     * @return 分页结果
     */
    PageResponseVO<SessionHistoryVO> getSessionHistory(AppUserDO userLogin, PageRequestVO<SessionHistoryReqVO> requestVO);

    /**
     * 分页查询订单历史
     * @param userLogin 当前登录用户
     * @param requestVO 分页请求参数
     * @return 分页结果
     */
    PageResponseVO<OrderHistoryVO> getOrderHistory(AppUserDO userLogin, PageRequestVO<OrderHistoryReqVO> requestVO);

    /**
     * 获取策略运行状态汇总
     * @param userLogin 当前登录用户
     * @param accountId 账号ID
     * @return 策略运行状态汇总
     */
    StrategyStatusVO getStrategyStatus(AppUserDO userLogin, Long accountId);

    /**
     * 查询持仓详情（包含策略业务信息）
     */
    List<PositionDetailVO> positionRiskDetail(AppUserDO userLogin, Long accountId);

    List<AppStrategyInstanceDO> strategyList(AppUserDO userLogin, Long accountId);

    void updateStrategy(AppUserDO userLogin, AccountStrategyUpdateReqVO reqVO);


}
