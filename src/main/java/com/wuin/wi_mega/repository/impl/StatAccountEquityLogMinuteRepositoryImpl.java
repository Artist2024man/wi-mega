package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.repository.StatAccountEquityLogMinuteRepository;
import com.wuin.wi_mega.repository.domain.StatAccountEquityLogMinuteDO;
import com.wuin.wi_mega.repository.mapper.StatAccountEquityLogMinuteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@Slf4j
public class StatAccountEquityLogMinuteRepositoryImpl extends ServiceImpl<StatAccountEquityLogMinuteMapper, StatAccountEquityLogMinuteDO> implements StatAccountEquityLogMinuteRepository {

    @Override
    public StatAccountEquityLogMinuteDO getByAccountIdAndTime(Long accountId, Long timeLong) {
        LambdaQueryWrapper<StatAccountEquityLogMinuteDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatAccountEquityLogMinuteDO::getAccountId, accountId);
        queryWrapper.eq(StatAccountEquityLogMinuteDO::getTimeLong, timeLong);
        return getOne(queryWrapper);
    }

    @Override
    public StatAccountEquityLogMinuteDO getLast(Long accountId) {
        LambdaQueryWrapper<StatAccountEquityLogMinuteDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatAccountEquityLogMinuteDO::getAccountId, accountId);
        queryWrapper.orderByDesc(StatAccountEquityLogMinuteDO::getTimeLong);
        queryWrapper.last("limit 1");
        return getOne(queryWrapper);
    }

    @Override
    public List<StatAccountEquityLogMinuteDO> listByTimeRange(Long userId, Long accountId, Long start, Long end) {
        LambdaQueryWrapper<StatAccountEquityLogMinuteDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatAccountEquityLogMinuteDO::getUserId, userId);
        queryWrapper.eq(StatAccountEquityLogMinuteDO::getAccountId, accountId);
        queryWrapper.ge(StatAccountEquityLogMinuteDO::getTimeLong, start);
        queryWrapper.le(StatAccountEquityLogMinuteDO::getTimeLong, end);
        queryWrapper.orderByAsc(StatAccountEquityLogMinuteDO::getTimeLong);
        return list(queryWrapper);
    }

    @Override
    public List<StatAccountEquityLogMinuteDO> listNeedSync(Long minId, Long minMinutes) {
        LambdaQueryWrapper<StatAccountEquityLogMinuteDO> queryWrapper = new LambdaQueryWrapper<>();
        if (null != minId) {
            queryWrapper.gt(StatAccountEquityLogMinuteDO::getId, minId);
        }
        if (null != minMinutes) {
            queryWrapper.ge(StatAccountEquityLogMinuteDO::getTimeLong, minMinutes);
        }
        return list(queryWrapper);
    }

    @Override
    public BigDecimal sumByTimeLong(Long userId, String exchange, Long timeLong) {
        return baseMapper.sumByTimeLong(userId, exchange, timeLong);
    }
}
