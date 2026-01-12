package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.repository.domain.AppAccountOrderDO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AppAccountOrderRepository extends IService<AppAccountOrderDO> {

    List<AppAccountOrderDO> listBySessionId(Long sessionId);

    List<AppAccountOrderDO> listByOrderIds(Long accountId, Set<Long> orderIdSet);

    /**
     * 分页查询订单历史
     * @param accountId 账号ID
     * @param sessionId 会话ID（可选）
     * @param orderType 订单类型（可选）
     * @param status 状态（可选）
     * @param positionSide 持仓方向（可选）
     * @param symbol 交易对（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<AppAccountOrderDO> pageList(Long accountId, Long sessionId, Integer orderType, Integer status,
                                      String positionSide, String symbol, LocalDateTime startTime,
                                      LocalDateTime endTime, int page, int pageSize);

}
