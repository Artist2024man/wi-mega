package com.wuin.wi_mega.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.wuin.wi_mega.common.enums.SessionBizStatusEnum;
import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.common.util.DateUtils;
import com.wuin.wi_mega.mega_market.model.Signal;
import com.wuin.wi_mega.mega_market.model.SignalResult;
import com.wuin.wi_mega.model.bo.signal.SessionMartingaleRunningBO;
import com.wuin.wi_mega.model.bo.base.StrategyMartingaleBaseParam;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MartingaleSignalStrategy extends MartingaleAppendStrategy {

    @Override
    public void handlerRunningSignal(SessionMartingaleRunningBO signalBO) {
        SessionBizStatusEnum statusEnum = signalBO.getStatus();
        long start = DateUtils.getMinuteStartMillis(LocalDateTime.now());
        if (SessionBizStatusEnum.WAIT_PROFIT.equals(statusEnum)) {
            AppAccountSessionDO sessionDO = appAccountSessionRepository.getById(signalBO.getId());
            StrategyMartingaleBaseParam baseParam = JSON.parseObject(sessionDO.getBaseParam(), StrategyMartingaleBaseParam.class);
            SignalResult result = this.requestSignal(sessionDO.getId(), signalBO.getSymbol(), baseParam, signalBO.getOpenPosition(), true, true, start);
//            log.warn("handlerRunningSignal -> sessionId={}, 获取到信号:{}", signalBO.getId(), JSON.toJSONString(result));
            if (Signal.CLOSE_LONG.equals(result.getSignal())) {
                if ("LONG".equals(signalBO.getOpenPosition())) {
                    super.takeProfit(signalBO.getId(), signalBO, signalBO.getOpenPosition());
                }
            } else if (Signal.CLOSE_SHORT.equals(result.getSignal())) {
                if ("SHORT".equals(signalBO.getOpenPosition())) {
                    super.takeProfit(signalBO.getId(), signalBO, signalBO.getOpenPosition());
                }
            }
        }
    }

    @Override
    public StrategyEnum strategy() {
        return StrategyEnum.MARTINGALE_SIGNAL;
    }

}
