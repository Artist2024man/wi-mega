package com.wuin.wi_mega.repository;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.model.bo.AccountCountBO;
import com.wuin.wi_mega.model.bo.AccountEquityStatBO;
import com.wuin.wi_mega.model.vo.AppUserListReqVO;
import com.wuin.wi_mega.repository.domain.AppUserDO;

import java.util.List;

public interface AppUserRepository extends IService<AppUserDO> {

    AppUserDO getByUsername(String username);

    IPage<AppUserDO> pageList(AppUserListReqVO param, Integer page, Integer pageSize);

    List<AccountCountBO> statAccountCountByUserIds(List<Long> userIds);

    List<AccountEquityStatBO> statEquitySumByUserIds(List<Long> userIds);

}
