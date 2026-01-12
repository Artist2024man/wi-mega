package com.wuin.wi_mega.model.bo.signal;

import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class SessionMartingaleRunningBO extends SessionRunningSignalBO {

    public SessionMartingaleRunningBO(AppAccountSessionDO sessionDO) {
        super(sessionDO);
    }


}
