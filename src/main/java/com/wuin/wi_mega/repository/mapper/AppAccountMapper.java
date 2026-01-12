package com.wuin.wi_mega.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuin.wi_mega.repository.domain.AppAccountDO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


public interface AppAccountMapper extends BaseMapper<AppAccountDO> {

    BigDecimal sumByUserId(@Param("userId") Long userId, @Param("exchange") String exchange);

    List<Long> listUserIds();

}
