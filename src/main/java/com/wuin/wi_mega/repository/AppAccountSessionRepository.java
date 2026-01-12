package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.model.bo.SessionAmtBO;
import com.wuin.wi_mega.model.bo.StrategyStatBO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AppAccountSessionRepository extends IService<AppAccountSessionDO> {

    List<AppAccountSessionDO> listByAccountIdsAndStatusList(Collection<Long> accountIds, List<Integer> statusList);


    List<AppAccountSessionDO> listByNextCheckTimeAndStatusList(LocalDateTime currentTime, List<Integer> statusList);

    List<AppAccountSessionDO> listByAccountIdAndStatusList(Long accountId, List<Integer> statusList);

    List<AppAccountSessionDO> listByAccountIdAndStatusListNoCache(Long accountId, List<Integer> statusList);

    List<Long> listIdBySymbol(SymbolEnum symbol, List<Integer> statusList);

    List<AppAccountSessionDO> listNeedSyncByStatusList(List<Integer> statusList, LocalDateTime minUpdateTime);

    AppAccountSessionDO getLastByAccountId(Long accountId);

    SessionAmtBO sumAmtByAccount(Long accountId, LocalDateTime minCreateTime, Long minId, List<Integer> statusList);

    /**
     * 分页查询会话历史
     * @param accountId 账号ID
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param symbol 交易对
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<AppAccountSessionDO> pageList(Long accountId, Integer status, LocalDateTime startTime,
                                        LocalDateTime endTime, String symbol, int page, int pageSize);

    /**
     * 统计账号的策略运行数据
     * @param accountId 账号ID
     * @param minCreateTime 最小创建时间（用于筛选今日数据，传null则统计全部）
     * @param statusList 状态列表
     * @return 统计结果
     */
    StrategyStatBO statByAccount(Long accountId, LocalDateTime minCreateTime, List<Integer> statusList);

}
