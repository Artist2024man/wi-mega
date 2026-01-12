package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.model.vo.AppStrategyInstanceListReqVO;
import com.wuin.wi_mega.repository.domain.AppStrategyInstanceDO;

import java.util.List;

public interface AppStrategyInstanceRepository extends IService<AppStrategyInstanceDO> {

    AppStrategyInstanceDO getById(Long id);

    /**
     * 分页查询策略实例
     * @param param 查询条件
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<AppStrategyInstanceDO> pageList(AppStrategyInstanceListReqVO param, Integer page, Integer pageSize);

    List<AppStrategyInstanceDO> listByStatus(Integer status);

}
