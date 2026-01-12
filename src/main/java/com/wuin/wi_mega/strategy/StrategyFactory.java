package com.wuin.wi_mega.strategy;

import com.alibaba.fastjson2.JSON;
import com.wuin.wi_mega.common.cache.local.StrategySignalCache;
import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.model.bo.signal.StrategyStartSignalBO;
import com.wuin.wi_mega.repository.AppStrategyInstanceRepository;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import com.wuin.wi_mega.repository.domain.AppStrategyInstanceDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StrategyFactory implements InitializingBean {

    @Autowired
    private List<TradeStrategy> strategyList;

    private Map<StrategyEnum, TradeStrategy> strategyMap = new HashMap<>();

    @Autowired
    private StrategySignalCache strategySignalCache;

    @Autowired
    private AppStrategyInstanceRepository appStrategyInstanceRepository;

    public void execute(AppAccountDO accountDO) {

        AppStrategyInstanceDO instanceDO = appStrategyInstanceRepository.getById(accountDO.getStrategyInstanceId());

        if (null == instanceDO) {
            log.warn("StrategyFactory.execute: instanceDO is null, accountId={}", accountDO.getId());
            return;
        }

        SymbolEnum symbol = SymbolEnum.valueOf(instanceDO.getSymbol());

        for (TradeStrategy strategy : strategyList) {
            if (!strategy.canExecute()) {
                log.info("strategy [{}] can not execute", strategy.strategy());
                continue;
            }
            if (strategy.strategy().name().equals(instanceDO.getCode())) {
                StrategyStartSignalBO signalBO = strategy.execute(accountDO, instanceDO, symbol);
                if (signalBO != null) {
                    if (signalBO.randomByScore()) {
                        strategySignalCache.save(signalBO);
                    } else {
                        log.warn("StrategyFactory -> 信号当前随机值结果为不执行，忽略本次信号:{}", JSON.toJSONString(signalBO));
                    }
                }
                break;
            }
        }
    }

    public void handlerRunningSignal(AppAccountSessionDO sessionDO) {
        TradeStrategy strategy = strategyMap.get(StrategyEnum.valueOf(sessionDO.getStrategyCode()));
        if (null == strategy) {
            log.warn("StrategyFactory.handlerRunningSignal: strategy is null, sessionId={}, strategy={}", sessionDO.getId(), strategy);
            return;
        }
        strategy.handlerRunningSignal(sessionDO);
    }

    public void handlerStartSignal(StrategyStartSignalBO signalBO) {
        TradeStrategy strategy = strategyMap.get(signalBO.getStrategy());
        if (null == strategy) {
            log.warn("StrategyFactory.handlerStartSignal: strategy is null, signal={}", JSON.toJSONString(signalBO));
            return;
        }
        strategy.handlerStartSignal(signalBO);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isEmpty(strategyList)) {
            return;
        }

        for (TradeStrategy tradeStrategy : strategyList) {
            strategyMap.put(tradeStrategy.strategy(), tradeStrategy);
        }
    }

}
