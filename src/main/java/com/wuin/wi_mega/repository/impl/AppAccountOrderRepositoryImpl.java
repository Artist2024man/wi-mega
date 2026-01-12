package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.repository.AppAccountOrderRepository;
import com.wuin.wi_mega.repository.domain.AppAccountOrderDO;
import com.wuin.wi_mega.repository.mapper.AppAccountOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
@Slf4j
public class AppAccountOrderRepositoryImpl extends ServiceImpl<AppAccountOrderMapper, AppAccountOrderDO> implements AppAccountOrderRepository {


    @Override
    public List<AppAccountOrderDO> listBySessionId(Long sessionId) {
        LambdaQueryWrapper<AppAccountOrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppAccountOrderDO::getSessionId, sessionId);
        return list(wrapper);
    }

    @Override
    public List<AppAccountOrderDO> listByOrderIds(Long accountId, Set<Long> orderIdSet) {
        LambdaQueryWrapper<AppAccountOrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AppAccountOrderDO::getOrderId, orderIdSet);
        wrapper.eq(AppAccountOrderDO::getAccountId, accountId);
        return list(wrapper);
    }

    @Override
    public IPage<AppAccountOrderDO> pageList(Long accountId, Long sessionId, Integer orderType, Integer status,
                                             String positionSide, String symbol, LocalDateTime startTime,
                                             LocalDateTime endTime, int page, int pageSize) {
        LambdaQueryWrapper<AppAccountOrderDO> wrapper = new LambdaQueryWrapper<>();

        // 账号ID必填
        wrapper.eq(AppAccountOrderDO::getAccountId, accountId);

        // 可选条件
        if (sessionId != null) {
            wrapper.eq(AppAccountOrderDO::getSessionId, sessionId);
        }
        if (orderType != null) {
            wrapper.eq(AppAccountOrderDO::getOrderType, orderType);
        }
        if (status != null) {
            wrapper.eq(AppAccountOrderDO::getStatus, status);
        }
        if (positionSide != null && !positionSide.isEmpty()) {
            wrapper.eq(AppAccountOrderDO::getPositionSide, positionSide);
        }
        if (symbol != null && !symbol.isEmpty()) {
            wrapper.eq(AppAccountOrderDO::getSymbol, symbol);
        }
        if (startTime != null) {
            wrapper.ge(AppAccountOrderDO::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AppAccountOrderDO::getCreateTime, endTime);
        }

        // 按创建时间倒序
        wrapper.orderByDesc(AppAccountOrderDO::getId);

        // 分页查询
        Page<AppAccountOrderDO> pageParam = new Page<>(page, pageSize);
        return page(pageParam, wrapper);
    }
}
