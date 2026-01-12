package com.wuin.wi_mega.model.bo;

import lombok.Data;

@Data
public class AccountCountBO {
    private Long userId;
    private Integer totalAccountCount;
    private Integer runningAccountCount;
}