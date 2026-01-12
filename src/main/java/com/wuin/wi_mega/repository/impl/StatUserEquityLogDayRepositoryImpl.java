package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.repository.StatUserEquityLogDayRepository;
import com.wuin.wi_mega.repository.domain.StatUserEquityLogDayDO;
import com.wuin.wi_mega.repository.mapper.StatUserEquityLogDayMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class StatUserEquityLogDayRepositoryImpl extends ServiceImpl<StatUserEquityLogDayMapper, StatUserEquityLogDayDO> implements StatUserEquityLogDayRepository {

    @Override
    public StatUserEquityLogDayDO getByAccountIdAndTime(Long userId, Long timeLong) {
        LambdaQueryWrapper<StatUserEquityLogDayDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogDayDO::getUserId, userId);
        queryWrapper.eq(StatUserEquityLogDayDO::getTimeLong, timeLong);
        return getOne(queryWrapper);
    }

    @Override
    public List<StatUserEquityLogDayDO> listByTimeRange(Long userId, String exchange, Long start, Long end) {
        LambdaQueryWrapper<StatUserEquityLogDayDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogDayDO::getUserId, userId);
        if (StringUtils.isNotBlank(exchange)) {
            queryWrapper.eq(StatUserEquityLogDayDO::getExchange, exchange);
        }
        queryWrapper.ge(StatUserEquityLogDayDO::getTimeLong, start);
        queryWrapper.le(StatUserEquityLogDayDO::getTimeLong, end);
        queryWrapper.orderByAsc(StatUserEquityLogDayDO::getTimeLong);
        return list(queryWrapper);
    }

    @Override
    public StatUserEquityLogDayDO getLast(Long userId, String exchange) {
        LambdaQueryWrapper<StatUserEquityLogDayDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogDayDO::getUserId, userId);
        queryWrapper.eq(StatUserEquityLogDayDO::getExchange, exchange);
        queryWrapper.orderByDesc(StatUserEquityLogDayDO::getId);
        queryWrapper.last("limit 1");
        return getOne(queryWrapper);
    }

    @Override
    public StatUserEquityLogDayDO getByTimeLong(Long userId, String exchange, Long timeLong) {
        LambdaQueryWrapper<StatUserEquityLogDayDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogDayDO::getUserId, userId);
        queryWrapper.eq(StatUserEquityLogDayDO::getExchange, exchange);
        queryWrapper.eq(StatUserEquityLogDayDO::getTimeLong, timeLong);
        return getOne(queryWrapper);
    }
}
