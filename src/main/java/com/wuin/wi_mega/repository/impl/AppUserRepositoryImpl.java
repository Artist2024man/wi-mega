package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.model.bo.AccountCountBO;
import com.wuin.wi_mega.model.bo.AccountEquityStatBO;
import com.wuin.wi_mega.model.vo.AppUserListReqVO;
import com.wuin.wi_mega.repository.AppUserRepository;
import com.wuin.wi_mega.repository.domain.AppUserDO;
import com.wuin.wi_mega.repository.mapper.AppUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class AppUserRepositoryImpl extends ServiceImpl<AppUserMapper, AppUserDO> implements AppUserRepository {

    @Override
    public AppUserDO getByUsername(String username) {
        LambdaQueryWrapper<AppUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppUserDO::getUsername, username);
        return getOne(queryWrapper);
    }

    @Override
    public IPage<AppUserDO> pageList(AppUserListReqVO param, Integer page, Integer pageSize) {
        LambdaQueryWrapper<AppUserDO> queryWrapper = new LambdaQueryWrapper<>();
        if (null != param) {
            if (StringUtils.isNotBlank(param.getUsername())) {
                queryWrapper.like(AppUserDO::getUsername, param.getUsername());
            }
            if (StringUtils.isNotBlank(param.getName())) {
                queryWrapper.like(AppUserDO::getName, "%" + param.getName() + "%");
            }
            if (null != param.getStatus()) {
                queryWrapper.eq(AppUserDO::getStatus, param.getStatus());
            }
        }
        queryWrapper.orderByDesc(AppUserDO::getId);
        return page(new Page<>(page, pageSize), queryWrapper);
    }

    public List<AccountCountBO> statAccountCountByUserIds(List<Long> userIds) {
        return baseMapper.statAccountCountByUserIds(userIds);
    }

    public List<AccountEquityStatBO> statEquitySumByUserIds(List<Long> userIds) {
        return baseMapper.statEquitySumByUserIds(userIds);
    }
}
