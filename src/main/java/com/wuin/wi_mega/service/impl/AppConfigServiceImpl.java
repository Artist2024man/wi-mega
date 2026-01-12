package com.wuin.wi_mega.service.impl;

import com.alibaba.fastjson2.JSON;
import com.wuin.wi_mega.repository.AppConfigRepository;
import com.wuin.wi_mega.repository.domain.AppConfigDO;
import com.wuin.wi_mega.service.AppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AppConfigServiceImpl implements AppConfigService {

    @Autowired
    private AppConfigRepository appConfigRepository;

    @Override
    public <T> List<T> getList(String paramKey, Class<T> clazz) {
        AppConfigDO configDO = appConfigRepository.getByParamKey(paramKey);
        if (null == configDO) {
            return null;
        }
        return JSON.parseArray(configDO.getParamValue(), clazz);
    }
}
