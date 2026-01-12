package com.wuin.wi_mega.binance.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FuturesAccountAssetDTO {

    /** 账户别名 */
    private String accountAlias;

    /** 资产类型，如 USDT */
    private String asset;

    /** 钱包余额 */
    private BigDecimal balance;

    /** 全仓钱包余额 */
    private BigDecimal crossWalletBalance;

    /** 全仓未实现盈亏 */
    private BigDecimal crossUnPnl;

    /** 可用余额 */
    private BigDecimal availableBalance;

    /** 最大可提金额 */
    private BigDecimal maxWithdrawAmount;

    /** 是否有可用保证金 */
    private Boolean marginAvailable;

    /** 更新时间（毫秒时间戳） */
    private Long updateTime;
}
