package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.model.vo.AppStrategyInstanceListReqVO;
import com.wuin.wi_mega.repository.AppStrategyInstanceRepository;
import com.wuin.wi_mega.repository.domain.AppStrategyInstanceDO;
import com.wuin.wi_mega.repository.mapper.AppStrategyInstanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

@Repository
@Slf4j
public class AppStrategyInstanceRepositoryImpl extends ServiceImpl<AppStrategyInstanceMapper, AppStrategyInstanceDO> implements AppStrategyInstanceRepository {

    @Override
    public AppStrategyInstanceDO getById(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public IPage<AppStrategyInstanceDO> pageList(AppStrategyInstanceListReqVO param, Integer page, Integer pageSize) {
        LambdaQueryWrapper<AppStrategyInstanceDO> wrapper = new LambdaQueryWrapper<>();
        if (null != param) {
            if (null != param.getStrategyId()) {
                wrapper.eq(AppStrategyInstanceDO::getStrategyId, param.getStrategyId());
            }
            if (StringUtils.isNotBlank(param.getSymbol())) {
                wrapper.eq(AppStrategyInstanceDO::getSymbol, param.getSymbol());
            }
            if (StringUtils.isNotBlank(param.getName())) {
                wrapper.like(AppStrategyInstanceDO::getName, "%" + param.getName() + "%");
            }
            if (StringUtils.isNotBlank(param.getExchange())) {
                wrapper.eq(AppStrategyInstanceDO::getExchange, param.getExchange());
            }
            // 新增：状态筛选
            if (null != param.getStatus()) {
                wrapper.eq(AppStrategyInstanceDO::getStatus, param.getStatus());
            }
        }
        wrapper.orderByDesc(AppStrategyInstanceDO::getUpdateTime);

        return page(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public List<AppStrategyInstanceDO> listByStatus(Integer status) {
        LambdaQueryWrapper<AppStrategyInstanceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppStrategyInstanceDO::getStatus, status);
        return this.list(wrapper);
    }

    @Override
    @CacheEvict(cacheNames = "strategyInstanceCache", key = "#id")
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @CacheEvict(cacheNames = "strategyInstanceCache", key = "#entity.id")
    public boolean save(AppStrategyInstanceDO entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(cacheNames = "strategyInstanceCache", key = "#entity.id")
    public boolean updateById(AppStrategyInstanceDO entity) {
        return super.updateById(entity);
    }

    @Override
    @Cacheable(cacheNames = "strategyInstanceCache", key = "#id")
    public AppStrategyInstanceDO getById(Serializable id) {
        return super.getById(id);
    }
}
