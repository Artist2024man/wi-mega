package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.repository.StatUserEquityLogHourRepository;
import com.wuin.wi_mega.repository.domain.StatUserEquityLogHourDO;
import com.wuin.wi_mega.repository.mapper.StatUserEquityLogHourMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class StatUserEquityLogHourRepositoryImpl extends ServiceImpl<StatUserEquityLogHourMapper, StatUserEquityLogHourDO> implements StatUserEquityLogHourRepository {

    @Override
    public StatUserEquityLogHourDO getByAccountIdAndTime(Long userId, Long timeLong) {
        LambdaQueryWrapper<StatUserEquityLogHourDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogHourDO::getUserId, userId);
        queryWrapper.eq(StatUserEquityLogHourDO::getTimeLong, timeLong);
        return getOne(queryWrapper);
    }

    @Override
    public List<StatUserEquityLogHourDO> listByTimeRange(Long userId, String exchange, Long start, Long end) {
        LambdaQueryWrapper<StatUserEquityLogHourDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogHourDO::getUserId, userId);
        if (StringUtils.isNotBlank(exchange)) {
            queryWrapper.eq(StatUserEquityLogHourDO::getExchange, exchange);
        }
        queryWrapper.ge(StatUserEquityLogHourDO::getTimeLong, start);
        queryWrapper.le(StatUserEquityLogHourDO::getTimeLong, end);
        queryWrapper.orderByAsc(StatUserEquityLogHourDO::getTimeLong);
        return list(queryWrapper);
    }

    @Override
    public StatUserEquityLogHourDO getLast(Long userId, String exchange) {
        LambdaQueryWrapper<StatUserEquityLogHourDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogHourDO::getUserId, userId);
        queryWrapper.eq(StatUserEquityLogHourDO::getExchange, exchange);
        queryWrapper.orderByDesc(StatUserEquityLogHourDO::getId);
        queryWrapper.last("limit 1");
        return getOne(queryWrapper);
    }

    @Override
    public StatUserEquityLogHourDO getByTimeLong(Long userId, String exchange, Long timeLong) {
        LambdaQueryWrapper<StatUserEquityLogHourDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogHourDO::getUserId, userId);
        queryWrapper.eq(StatUserEquityLogHourDO::getExchange, exchange);
        queryWrapper.eq(StatUserEquityLogHourDO::getTimeLong, timeLong);
        return getOne(queryWrapper);
    }

}
