package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.repository.domain.StatAccountEquityLogMinuteDO;

import java.math.BigDecimal;
import java.util.List;

public interface StatAccountEquityLogMinuteRepository extends IService<StatAccountEquityLogMinuteDO> {

    StatAccountEquityLogMinuteDO getByAccountIdAndTime(Long accountId, Long timeLong);

    StatAccountEquityLogMinuteDO getLast(Long accountId);

    List<StatAccountEquityLogMinuteDO> listByTimeRange(Long userId, Long accountId, Long start, Long end);

    List<StatAccountEquityLogMinuteDO> listNeedSync(Long minId, Long minMinutes);


    BigDecimal sumByTimeLong(Long userId, String exchange, Long timeLong);

}
