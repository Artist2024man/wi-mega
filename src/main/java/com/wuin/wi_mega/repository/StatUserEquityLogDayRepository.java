package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.repository.domain.StatUserEquityLogDayDO;

import java.util.List;

public interface StatUserEquityLogDayRepository extends IService<StatUserEquityLogDayDO> {

    StatUserEquityLogDayDO getByAccountIdAndTime(Long id, Long timeLong);

    List<StatUserEquityLogDayDO> listByTimeRange(Long userId, String exchange, Long start, Long end);

    StatUserEquityLogDayDO getLast(Long userId, String exchange);

    StatUserEquityLogDayDO getByTimeLong(Long userId, String exchange, Long timeLong);
}
