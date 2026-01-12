package com.wuin.wi_mega.model.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SessionAmtBO {

    private Long maxId;

    private BigDecimal closePnl;

    private BigDecimal openFee;

    private BigDecimal closeFee;

}
