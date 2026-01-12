package com.wuin.wi_mega.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuin.wi_mega.repository.AppStrategyRepository;
import com.wuin.wi_mega.repository.domain.AppStrategyDO;
import com.wuin.wi_mega.repository.mapper.AppStrategyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class AppStrategyRepositoryImpl extends ServiceImpl<AppStrategyMapper, AppStrategyDO> implements AppStrategyRepository {


}
