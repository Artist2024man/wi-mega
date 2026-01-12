package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.repository.domain.AppAccountDO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AppAccountRepository extends IService<AppAccountDO> {

    List<AppAccountDO> listByStatus(Integer strategyStatus);

    AppAccountDO getByApiKey(String apiKey);

    IPage<AppAccountDO> pageListByUserId(Long userId, Integer page, Integer pageSize);


    List<Long> listUserIds();


    BigDecimal sumByUserId(Long userId, String exchange);

    List<AppAccountDO> listNeedSync(LocalDateTime currentTime);


    List<AppAccountDO> listByStrategyInstanceId(Long strategyInstanceId, Integer strategyStatus);

    /**
     * 统计用户下绑定的账号数量
     * @param userId 用户ID
     * @return 账号数量
     */
    long countByUserId(Long userId);

}

