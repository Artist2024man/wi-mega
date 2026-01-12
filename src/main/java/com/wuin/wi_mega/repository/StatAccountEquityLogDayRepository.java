package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.repository.domain.StatAccountEquityLogDayDO;

import java.math.BigDecimal;
import java.util.List;

public interface StatAccountEquityLogDayRepository extends IService<StatAccountEquityLogDayDO> {

    StatAccountEquityLogDayDO getByAccountIdAndTime(Long id, Long timeLong);

    List<StatAccountEquityLogDayDO> listByTimeRange(Long userId, Long accountId, Long start, Long end);

    BigDecimal sumByTimeLong(Long userId, String exchange, Long timeLong);

}
