package com.wuin.wi_mega.service;


import com.wuin.wi_mega.model.bo.AccountEquityAddBO;
import com.wuin.wi_mega.repository.domain.AppAccountDO;

public interface AccountEquityService {

    void addLog(AppAccountDO accountDO, AccountEquityAddBO addBO);

    void sync();

}
