package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.repository.AppConfigRepository;
import com.wuin.wi_mega.repository.domain.AppConfigDO;
import com.wuin.wi_mega.repository.mapper.AppConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class AppConfigRepositoryImpl extends ServiceImpl<AppConfigMapper, AppConfigDO> implements AppConfigRepository {

    @Override
    @Cacheable(cacheNames = "appConfigCache", key = "#paramKey")
    public AppConfigDO getByParamKey(String paramKey) {
        LambdaQueryWrapper<AppConfigDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppConfigDO::getParamKey, paramKey);
        return getOne(queryWrapper);
    }

}
