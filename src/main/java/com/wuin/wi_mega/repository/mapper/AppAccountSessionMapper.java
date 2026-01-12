package com.wuin.wi_mega.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuin.wi_mega.model.bo.SessionAmtBO;
import com.wuin.wi_mega.model.bo.StrategyStatBO;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface AppAccountSessionMapper extends BaseMapper<AppAccountSessionDO> {

    List<Long> listIdBySymbol(@Param("symbol") String symbol, @Param("statusList") List<Integer> statusList);

    SessionAmtBO sumAmtByAccount(@Param("accountId") Long accountId, @Param("minCreateTime") LocalDateTime minCreateTime,
                                 @Param("minId") Long minId,
                                 @Param("statusList") List<Integer> statusList);

    /**
     * 统计账号的策略运行数据
     * @param accountId 账号ID
     * @param minCreateTime 最小创建时间（用于筛选今日数据）
     * @param statusList 状态列表
     * @return 统计结果
     */
    StrategyStatBO statByAccount(@Param("accountId") Long accountId,
                                 @Param("minCreateTime") LocalDateTime minCreateTime,
                                 @Param("statusList") List<Integer> statusList);

}
