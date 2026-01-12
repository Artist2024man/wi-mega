package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.repository.domain.StatUserEquityLogHourDO;

import java.util.List;

public interface StatUserEquityLogHourRepository extends IService<StatUserEquityLogHourDO> {

    StatUserEquityLogHourDO getByAccountIdAndTime(Long userId, Long timeLong);

    List<StatUserEquityLogHourDO> listByTimeRange(Long userId, String exchange, Long start, Long end);

    StatUserEquityLogHourDO getLast(Long userId, String exchange);

    StatUserEquityLogHourDO getByTimeLong(Long userId, String exchange, Long timeLong);

}
