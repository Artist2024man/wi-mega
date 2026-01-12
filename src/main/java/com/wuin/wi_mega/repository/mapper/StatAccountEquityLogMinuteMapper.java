package com.wuin.wi_mega.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuin.wi_mega.repository.domain.StatAccountEquityLogMinuteDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;


@Repository
public interface StatAccountEquityLogMinuteMapper extends BaseMapper<StatAccountEquityLogMinuteDO> {

    BigDecimal sumByTimeLong(@Param("userId") Long userId, @Param("exchange") String exchange, @Param("timeLong") Long timeLong);

}
