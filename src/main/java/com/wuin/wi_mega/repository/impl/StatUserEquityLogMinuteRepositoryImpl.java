package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.repository.StatUserEquityLogMinuteRepository;
import com.wuin.wi_mega.repository.domain.StatUserEquityLogMinuteDO;
import com.wuin.wi_mega.repository.mapper.StatUserEquityLogMinuteMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@Slf4j
public class StatUserEquityLogMinuteRepositoryImpl extends ServiceImpl<StatUserEquityLogMinuteMapper, StatUserEquityLogMinuteDO> implements StatUserEquityLogMinuteRepository {

    @Override
    public StatUserEquityLogMinuteDO getByAccountIdAndTime(Long userId, Long timeLong) {
        LambdaQueryWrapper<StatUserEquityLogMinuteDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogMinuteDO::getUserId, userId);
        queryWrapper.eq(StatUserEquityLogMinuteDO::getTimeLong, timeLong);
        return getOne(queryWrapper);
    }

    @Override
    public List<StatUserEquityLogMinuteDO> listByTimeRange(Long userId, String exchange, Long start, Long end) {
        LambdaQueryWrapper<StatUserEquityLogMinuteDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogMinuteDO::getUserId, userId);
        if (StringUtils.isNotBlank(exchange)) {
            queryWrapper.eq(StatUserEquityLogMinuteDO::getExchange, exchange);
        }
        queryWrapper.ge(StatUserEquityLogMinuteDO::getTimeLong, start);
        queryWrapper.le(StatUserEquityLogMinuteDO::getTimeLong, end);
        queryWrapper.orderByAsc(StatUserEquityLogMinuteDO::getTimeLong);
        return list(queryWrapper);
    }

    @Override
    public BigDecimal sumByTimeLong(Long userId, String exchange, Long timeLong) {
        return baseMapper.sumByTimeLong(userId, exchange, timeLong);
    }

    @Override
    public StatUserEquityLogMinuteDO getLast(Long userId, String exchange) {
        LambdaQueryWrapper<StatUserEquityLogMinuteDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogMinuteDO::getUserId, userId);
        queryWrapper.eq(StatUserEquityLogMinuteDO::getExchange, exchange);
        queryWrapper.orderByDesc(StatUserEquityLogMinuteDO::getId);
        queryWrapper.last("limit 1");
        return getOne(queryWrapper);
    }

    @Override
    public StatUserEquityLogMinuteDO getByTimeLong(Long userId, String exchange, Long timeLong) {
        LambdaQueryWrapper<StatUserEquityLogMinuteDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatUserEquityLogMinuteDO::getUserId, userId);
        queryWrapper.eq(StatUserEquityLogMinuteDO::getExchange, exchange);
        queryWrapper.eq(StatUserEquityLogMinuteDO::getTimeLong, timeLong);
        return getOne(queryWrapper);
    }
}
