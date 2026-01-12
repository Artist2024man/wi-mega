package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.common.enums.TradeTypeEnum;
import com.wuin.wi_mega.repository.AppAccountRepository;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.mapper.AppAccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Slf4j
public class AppAccountRepositoryImpl extends ServiceImpl<AppAccountMapper, AppAccountDO> implements AppAccountRepository {


    @Override
    public List<AppAccountDO> listByStatus(Integer strategyStatus) {
        LambdaQueryWrapper<AppAccountDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppAccountDO::getStrategyStatus, strategyStatus);
        return this.list(queryWrapper);
    }

    @Override
    public AppAccountDO getByApiKey(String apiKey) {
        LambdaQueryWrapper<AppAccountDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppAccountDO::getApiKey, apiKey);
        return this.getOne(queryWrapper);
    }

    @Override
    public IPage<AppAccountDO> pageListByUserId(Long userId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<AppAccountDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppAccountDO::getUserId, userId);
        queryWrapper.orderByDesc(AppAccountDO::getId);
        return page(new Page<>(page, pageSize), queryWrapper);
    }

    @Override
    public List<Long> listUserIds() {
        return baseMapper.listUserIds();
    }

    @Override
    public BigDecimal sumByUserId(Long userId, String exchange) {
        return baseMapper.sumByUserId(userId, exchange);
    }

    @Override
    public List<AppAccountDO> listNeedSync(LocalDateTime currentTime) {
        LambdaQueryWrapper<AppAccountDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(AppAccountDO::getNextSyncTime, currentTime);
        queryWrapper.eq(AppAccountDO::getTradeType, TradeTypeEnum.REAL.code());
        return list(queryWrapper);
    }

    @Override
    public List<AppAccountDO> listByStrategyInstanceId(Long strategyInstanceId, Integer strategyStatus) {
        LambdaQueryWrapper<AppAccountDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppAccountDO::getStrategyInstanceId, strategyInstanceId);
        queryWrapper.eq(AppAccountDO::getStrategyStatus, strategyStatus);
        return list(queryWrapper);
    }

    @Override
    @CacheEvict(cacheNames = "accountCache", key = "#entity.id")
    public boolean updateById(AppAccountDO entity) {
        return super.updateById(entity);
    }

    @Override
    @Cacheable(cacheNames = "accountCache", key = "#id")
    public AppAccountDO getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    public long countByUserId(Long userId) {
        LambdaQueryWrapper<AppAccountDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppAccountDO::getUserId, userId);
        return this.count(queryWrapper);
    }

}
