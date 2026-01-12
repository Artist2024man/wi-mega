package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.repository.StatAccountEquityLogDayRepository;
import com.wuin.wi_mega.repository.domain.StatAccountEquityLogDayDO;
import com.wuin.wi_mega.repository.mapper.StatAccountEquityLogDayMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@Slf4j
public class StatAccountEquityLogDayRepositoryImpl extends ServiceImpl<StatAccountEquityLogDayMapper, StatAccountEquityLogDayDO> implements StatAccountEquityLogDayRepository {

    @Override
    public StatAccountEquityLogDayDO getByAccountIdAndTime(Long accountId, Long timeLong) {
        LambdaQueryWrapper<StatAccountEquityLogDayDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatAccountEquityLogDayDO::getAccountId, accountId);
        queryWrapper.eq(StatAccountEquityLogDayDO::getTimeLong, timeLong);
        return getOne(queryWrapper);
    }

    @Override
    public List<StatAccountEquityLogDayDO> listByTimeRange(Long userId, Long accountId, Long start, Long end) {
        LambdaQueryWrapper<StatAccountEquityLogDayDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatAccountEquityLogDayDO::getUserId, userId);
        queryWrapper.eq(StatAccountEquityLogDayDO::getAccountId, accountId);
        queryWrapper.ge(StatAccountEquityLogDayDO::getTimeLong, start);
        queryWrapper.le(StatAccountEquityLogDayDO::getTimeLong, end);
        queryWrapper.orderByAsc(StatAccountEquityLogDayDO::getTimeLong);
        return list(queryWrapper);
    }

    @Override
    public BigDecimal sumByTimeLong(Long userId, String exchange, Long timeLong) {
        return baseMapper.sumByTimeLong(userId, exchange, timeLong);
    }
}
