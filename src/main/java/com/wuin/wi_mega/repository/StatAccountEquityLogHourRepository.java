package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.repository.domain.StatAccountEquityLogHourDO;

import java.math.BigDecimal;
import java.util.List;

public interface StatAccountEquityLogHourRepository extends IService<StatAccountEquityLogHourDO> {

    StatAccountEquityLogHourDO getByAccountIdAndTime(Long id, Long timeLong);

    List<StatAccountEquityLogHourDO> listByTimeRange(Long userId, Long accountId, Long start, Long end);

    BigDecimal sumByTimeLong(Long userId, String exchange, Long timeLong);
}
