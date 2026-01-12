package com.wuin.wi_mega.service;

import com.wuin.wi_mega.common.enums.SymbolEnum;

import java.math.BigDecimal;

public interface DepthService {

    BigDecimal getPrefectPrice(SymbolEnum symbol, BigDecimal price, BigDecimal stepPrice, boolean isBuy);

}
