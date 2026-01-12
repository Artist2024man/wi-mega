package com.wuin.wi_mega.model.bo.signal;

import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.mega_market.model.SignalResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyMartingaleStartSignalBO extends StrategyStartSignalBO {

    private SignalResult result;

    private Long start;

    public StrategyMartingaleStartSignalBO(Long accountId, SymbolEnum symbol, Long start, SignalResult result) {
        super(accountId, symbol, 100, StrategyEnum.MARTINGALE_APPEND);
        this.result = result;
        this.start = start;
    }


}
