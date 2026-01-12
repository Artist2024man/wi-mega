package com.wuin.wi_mega.model.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountEquityAddBO {

    private Long accountId;

    private BigDecimal equity;

    private LocalDateTime time;

}
