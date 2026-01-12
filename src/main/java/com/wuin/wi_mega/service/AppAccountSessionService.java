package com.wuin.wi_mega.service;

import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AppAccountSessionService {
    List<AppAccountSessionDO> listOpeningByAccountIds(Collection<Long> accountIds);

    List<AppAccountSessionDO> listNeedSync();

    void doFinalSync(AppAccountSessionDO sessionDO);


}
