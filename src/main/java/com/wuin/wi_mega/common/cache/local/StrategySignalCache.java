package com.wuin.wi_mega.common.cache.local;

import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.model.bo.signal.StrategyStartSignalBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class StrategySignalCache {

    private final Map<SymbolEnum, Set<StrategyStartSignalBO>> CACHE_MAP = new ConcurrentHashMap<>();

    public void save(StrategyStartSignalBO bo) {
        this.remove(bo);
        this.add(bo);
    }

    public void add(StrategyStartSignalBO bo) {
        if (bo == null || bo.getSymbol() == null) {
            return;
        }
        CACHE_MAP.computeIfAbsent(
                bo.getSymbol(),
                k -> ConcurrentHashMap.newKeySet()
        ).add(bo);
    }

    public void remove(StrategyStartSignalBO bo) {
        if (bo == null || bo.getSymbol() == null) {
            return;
        }
        Set<StrategyStartSignalBO> set = CACHE_MAP.get(bo.getSymbol());
        if (set == null) {
            return;
        }
        set.remove(bo);
        if (set.isEmpty()) {
            CACHE_MAP.remove(bo.getSymbol(), set);
        }
    }

    public Set<StrategyStartSignalBO> get(SymbolEnum symbol) {
        return CACHE_MAP.getOrDefault(symbol, Set.of());
    }
}
