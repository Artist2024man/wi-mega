package com.wuin.wi_mega.binance.bo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BinanceCommissionRateDTO {
    
    /**
     * 交易对，如 BTCUSDT
     */
    private String symbol;
    
    /**
     * 挂单手续费率 (Maker Commission Rate)
     * 例如：0.0002 = 0.02%
     * 挂单增加市场流动性时收取
     */
    private BigDecimal makerCommissionRate;
    
    /**
     * 吃单手续费率 (Taker Commission Rate)
     * 例如：0.0004 = 0.04%
     * 吃单消耗市场流动性时收取
     */
    private BigDecimal takerCommissionRate;
    
    /**
     * 返佣手续费率 (Referral Program Incentive Commission Rate)
     * 例如：0.00005 = 0.005%
     * 推荐返佣的费率
     */
    private BigDecimal rpiCommissionRate;
}