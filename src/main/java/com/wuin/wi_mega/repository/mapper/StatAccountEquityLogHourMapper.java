package com.wuin.wi_mega.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuin.wi_mega.repository.domain.StatAccountEquityLogHourDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;


@Repository
public interface StatAccountEquityLogHourMapper extends BaseMapper<StatAccountEquityLogHourDO> {

    BigDecimal sumByTimeLong(@Param("userId") Long userId, @Param("exchange") String exchange, @Param("timeLong") Long timeLong);

}
