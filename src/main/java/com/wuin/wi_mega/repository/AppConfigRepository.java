package com.wuin.wi_mega.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuin.wi_mega.repository.domain.AppConfigDO;

public interface AppConfigRepository extends IService<AppConfigDO> {
    AppConfigDO getByParamKey(String paramKey);

}
