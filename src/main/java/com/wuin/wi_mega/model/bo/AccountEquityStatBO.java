package com.wuin.wi_mega.model.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountEquityStatBO {
    private Long userId;
    private BigDecimal totalInitEquity;
    private BigDecimal totalCurEquity;
}