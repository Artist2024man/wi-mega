package com.wuin.wi_mega.model.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建账号请求")
public class AccountCreateReqVO {

    @Schema(description = "账号名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "KEY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiKey;

    @Schema(description = "密码/Secret", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("apiSecret")  // 兼容 apiSecret 字段名
    private String apiKeyPass;

    @Schema(description = "平台，默认BINANCE")
    private String exchange;

    @Schema(description = "初始净值，默认0")
    private BigDecimal initEquity;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "持仓模式：1=单向持仓，2=双向持仓，默认2")
    private Integer dualSidePosition;

    @Schema(description = "杠杆倍数，支持:1-125，默认20")
    private Integer leverage;

    @Schema(description = "策略实例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long strategyInstanceId;

    @Schema(description = "是否自动启动策略：true=创建后立即启动，false或不传=创建后不启动")
    private Boolean autoStart;

    @Schema(description = "交易模式：1=模拟，2=实盘，默认2")
    private Integer tradeType;

    // 兼容 apiSecret 字段的 setter 方法（备用方案，如果 @JsonAlias 不生效）
    public void setApiSecret(String apiSecret) {
        if (this.apiKeyPass == null) {
            this.apiKeyPass = apiSecret;
        }
    }

    // 提供默认值的 getter 方法
    public String getExchange() {
        return exchange == null ? "BINANCE" : exchange;
    }

    public BigDecimal getInitEquity() {
        return initEquity == null ? BigDecimal.ZERO : initEquity;
    }

    public Integer getDualSidePosition() {
        return dualSidePosition == null ? 2 : dualSidePosition;
    }

    public Integer getLeverage() {
        return leverage == null ? 20 : leverage;
    }

    public Integer getTradeType() {
        return tradeType == null ? 2 : tradeType;  // 默认实盘
    }
}
