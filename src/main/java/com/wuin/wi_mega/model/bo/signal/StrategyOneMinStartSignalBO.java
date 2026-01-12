package com.wuin.wi_mega.model.bo.signal;

import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyOneMinStartSignalBO extends StrategyStartSignalBO {

    private BigDecimal openPrice;

    private String position;

    private Long start;

    public StrategyOneMinStartSignalBO(Long accountId, SymbolEnum symbol, int score, BigDecimal openPrice, String position, Long start) {
        super(accountId, symbol, score, StrategyEnum.ONE_MIN_SHORT);
        this.openPrice = openPrice;
        this.position = position;
        this.start = start;
    }
}
