package com.wuin.wi_mega.binance.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepthLevel {
    private BigDecimal price;
    private BigDecimal quantity;
}

