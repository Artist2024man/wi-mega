package com.wuin.wi_mega.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuin.wi_mega.model.bo.AccountCountBO;
import com.wuin.wi_mega.model.bo.AccountEquityStatBO;
import com.wuin.wi_mega.repository.domain.AppUserDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppUserMapper extends BaseMapper<AppUserDO> {

    List<AccountCountBO> statAccountCountByUserIds(@Param("userIds") List<Long> userIds);

    List<AccountEquityStatBO> statEquitySumByUserIds(@Param("userIds") List<Long> userIds);

}
