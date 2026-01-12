package com.wuin.wi_mega.model.vo;

import com.wuin.wi_mega.common.enums.MockDataEnum;
import com.wuin.wi_mega.common.enums.TradeTypeEnum;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountResVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "账户名称")
    private String name;

    @Schema(description = "API Key")
    private String apiKey;

    @Schema(description = "API Secret")
    private String apiSecret;

    @Schema(description = "所属平台")
    private String exchange;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "策略实例ID")
    private Long strategyInstanceId;

    @Schema(description = "策略实例名称")
    private String strategyInstanceName;

    @Schema(description = "策略编码")
    private String strategyCode;

    @Schema(description = "策略状态, 1=运行中，0=已停止")
    private Integer strategyStatus;

    @Schema(description = "初始净值")
    private BigDecimal initEquity;

    @Schema(description = "当前净值")
    private BigDecimal curEquity;

    @Schema(description = "净值单位，支持：USDT,USDC,USD,USD1")
    private String equityCoin;

    @Schema(description = "总盈亏")
    private BigDecimal totalProfit;

    @Schema(description = "今日盈亏")
    private BigDecimal todayProfit;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "盈亏")
    private BigDecimal closePnl;

    @Schema(description = "开仓手续费")
    private BigDecimal openFee;

    @Schema(description = "完成手续费")
    private BigDecimal closeFee;

    @Schema(description = "今日盈亏")
    private BigDecimal todayClosePnl = BigDecimal.ZERO;

    @Schema(description = "今日开仓手续费")
    private BigDecimal todayOpenFee = BigDecimal.ZERO;

    @Schema(description = "今日完成手续费")
    private BigDecimal todayCloseFee = BigDecimal.ZERO;

    @Schema(description = "是否模拟运行")
    private Integer mockData;

    @Schema(description = "挂单手续费费率")
    private BigDecimal makerFeeRate;

    @Schema(description = "吃单手续费费率")
    private BigDecimal takerFeeRate;

    @Schema(description = "策略执行允许的最小价格")
    private BigDecimal strategyMinPrice;

    @Schema(description = "策略执行允许的最大价格")
    private BigDecimal strategyMaxPrice;

    @Schema(description = "杠杆倍数")
    private Integer leverage;

    public AccountResVO() {

    }

    public AccountResVO(AppAccountDO accountDO) {
        this.id = accountDO.getId();
        this.userId = accountDO.getUserId();
        this.name = accountDO.getName();
        this.exchange = accountDO.getExchange();
        this.symbol = accountDO.getSymbol();
        this.strategyInstanceId = accountDO.getStrategyInstanceId();
        this.strategyStatus = accountDO.getStrategyStatus();
        this.initEquity = accountDO.getInitEquity();
        this.curEquity = accountDO.getCurEquity();
        this.equityCoin = accountDO.getEquityCoin();
        this.remark = accountDO.getRemark();
        this.createTime = accountDO.getCreateTime();
        this.updateTime = accountDO.getUpdateTime();
        this.closePnl = accountDO.getClosePnl();
        this.openFee = accountDO.getOpenFee();
        this.closeFee = accountDO.getCloseFee();
        this.apiKey = accountDO.getApiKey();
        this.apiSecret = accountDO.getApiKeyPass();
        this.mockData = (null != accountDO.getTradeType() && TradeTypeEnum.MOCK.equalsByCode(accountDO.getTradeType())) ? MockDataEnum.MOCK.code() : MockDataEnum.REAL.code();
        this.makerFeeRate = accountDO.getMakerFeeRate();
        this.takerFeeRate = accountDO.getTakerFeeRate();
        this.strategyMinPrice = accountDO.getStrategyMinPrice();
        this.strategyMaxPrice = accountDO.getStrategyMaxPrice();
        this.leverage = accountDO.getLeverage();
    }
}
