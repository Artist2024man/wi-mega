package com.wuin.wi_mega.model.bo.signal;

import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyStartSignalBO {

    private Long accountId;

    private SymbolEnum symbol;

    private int score;

    //策略类型
    private StrategyEnum strategy;

    public Boolean randomByScore() {
        return score >= 70;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StrategyStartSignalBO that = (StrategyStartSignalBO) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(accountId);
    }
}
