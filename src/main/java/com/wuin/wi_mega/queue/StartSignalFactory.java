package com.wuin.wi_mega.queue;

import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.model.bo.signal.StrategyStartSignalBO;
import com.wuin.wi_mega.queue.impl.AccountStartSignalQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class StartSignalFactory implements InitializingBean {

    private final Map<SymbolEnum, AccountStartSignalQueue> QUEUE_MAP = new HashMap<>();


    public void offer(StrategyStartSignalBO value) {
        QUEUE_MAP.get(value.getSymbol()).offer(value);
    }

    /**
     * 阻塞式出队
     */
    public StrategyStartSignalBO take(SymbolEnum symbol) throws InterruptedException {
        return QUEUE_MAP.get(symbol).take();
    }

    public StrategyStartSignalBO poll(SymbolEnum symbol) {
        return QUEUE_MAP.get(symbol).poll();
    }

    public int size(SymbolEnum symbol) {
        return QUEUE_MAP.get(symbol).size();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        for (SymbolEnum value : SymbolEnum.values()) {
            QUEUE_MAP.put(value, new AccountStartSignalQueue());
        }
    }
}
