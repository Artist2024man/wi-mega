package com.wuin.wi_mega.repository.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wuin.wi_mega.repository.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("app_config")
public class AppConfigDO extends BaseDO {
    private String paramKey;
    private String paramValue;
    /**
     * 配置的状态，为0表示失效，为1表示有效
     */
    private Integer status;
}
