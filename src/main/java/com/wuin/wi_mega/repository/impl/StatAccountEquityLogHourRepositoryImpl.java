package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.repository.StatAccountEquityLogHourRepository;
import com.wuin.wi_mega.repository.domain.StatAccountEquityLogHourDO;
import com.wuin.wi_mega.repository.mapper.StatAccountEquityLogHourMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@Slf4j
public class StatAccountEquityLogHourRepositoryImpl extends ServiceImpl<StatAccountEquityLogHourMapper, StatAccountEquityLogHourDO> implements StatAccountEquityLogHourRepository {

    @Override
    public StatAccountEquityLogHourDO getByAccountIdAndTime(Long accountId, Long timeLong) {
        LambdaQueryWrapper<StatAccountEquityLogHourDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatAccountEquityLogHourDO::getAccountId, accountId);
        queryWrapper.eq(StatAccountEquityLogHourDO::getTimeLong, timeLong);
        return getOne(queryWrapper);
    }

    @Override
    public List<StatAccountEquityLogHourDO> listByTimeRange(Long userId, Long accountId, Long start, Long end) {
        LambdaQueryWrapper<StatAccountEquityLogHourDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatAccountEquityLogHourDO::getUserId, userId);
        queryWrapper.eq(StatAccountEquityLogHourDO::getAccountId, accountId);
        queryWrapper.ge(StatAccountEquityLogHourDO::getTimeLong, start);
        queryWrapper.le(StatAccountEquityLogHourDO::getTimeLong, end);
        queryWrapper.orderByAsc(StatAccountEquityLogHourDO::getTimeLong);
        return list(queryWrapper);
    }

    @Override
    public BigDecimal sumByTimeLong(Long userId, String exchange, Long timeLong) {
        return baseMapper.sumByTimeLong(userId, exchange, timeLong);
    }
}
