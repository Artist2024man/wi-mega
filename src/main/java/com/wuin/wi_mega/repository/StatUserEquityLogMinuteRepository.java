package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.repository.domain.StatUserEquityLogMinuteDO;

import java.math.BigDecimal;
import java.util.List;

public interface StatUserEquityLogMinuteRepository extends IService<StatUserEquityLogMinuteDO> {

    StatUserEquityLogMinuteDO getByAccountIdAndTime(Long userId, Long timeLong);

    List<StatUserEquityLogMinuteDO> listByTimeRange(Long userId, String exchange, Long start, Long end);

    BigDecimal sumByTimeLong(Long userId, String exchange, Long timeLong);

    StatUserEquityLogMinuteDO getLast(Long userId, String exchange);

    StatUserEquityLogMinuteDO getByTimeLong(Long userId, String exchange, Long timeLong);

}
