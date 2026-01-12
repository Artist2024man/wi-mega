package com.wuin.wi_mega.service;

import com.wuin.wi_mega.binance.bo.BinancePosition;
import com.wuin.wi_mega.model.vo.TradeReqVO;
import com.wuin.wi_mega.model.vo.TradeResVO;
import com.wuin.wi_mega.repository.domain.AppUserDO;

import java.util.List;

public interface TradeService {

    /**
     * 开多
     */
    TradeResVO openLong(AppUserDO userLogin, TradeReqVO reqVO);

    /**
     * 加多
     */
    TradeResVO addLong(AppUserDO userLogin, TradeReqVO reqVO);

    /**
     * 平多
     */
    TradeResVO closeLong(AppUserDO userLogin, TradeReqVO reqVO);

    /**
     * 开空
     */
    TradeResVO openShort(AppUserDO userLogin, TradeReqVO reqVO);

    /**
     * 加空
     */
    TradeResVO addShort(AppUserDO userLogin, TradeReqVO reqVO);

    /**
     * 平空
     */
    TradeResVO closeShort(AppUserDO userLogin, TradeReqVO reqVO);

    /**
     * 全部平仓
     */
    void closeAll(AppUserDO userLogin, Long accountId);

    /**
     * 查询持仓
     */
    List<BinancePosition> getPosition(AppUserDO userLogin, Long accountId);
}
