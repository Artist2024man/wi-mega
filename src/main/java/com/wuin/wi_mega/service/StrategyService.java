package com.wuin.wi_mega.service;

import com.wuin.wi_mega.model.PageRequestVO;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.vo.*;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import com.wuin.wi_mega.repository.domain.AppUserDO;

import java.util.List;

public interface StrategyService {

    /**
     * 启动策略
     */
    void start(AppAccountDO accountDO);

    /**
     * 停止策略
     */
    void stop(AppAccountDO accountDO);

    List<AppStrategyVO> listStrategy(AppUserDO userLogin);

    /**
     * 策略市场列表（用户端，只返回已上架的策略）
     */
    PageResponseVO<AppStrategyInstanceVO> listInstance(AppUserDO userLogin, PageRequestVO<AppStrategyInstanceListReqVO> requestVO);

    /**
     * 策略管理列表（管理端，返回所有策略）
     */
    PageResponseVO<AppStrategyInstanceVO> listInstanceForAdmin(AppUserDO userLogin, PageRequestVO<AppStrategyInstanceListReqVO> requestVO);

    Long createInstance(AppUserDO userLogin, AppStrategyInstanceCreateReqVO requestVO);

    void updateInstance(AppUserDO userLogin, AppStrategyInstanceCreateReqVO requestVO);

    /**
     * 策略上下架
     */
    void updateStatus(AppUserDO userLogin, AppStrategyInstanceStatusReqVO requestVO);

    void deleteInstance(AppUserDO userLogin, AppStrategyInstanceDeleteReqVO requestVO);
}
