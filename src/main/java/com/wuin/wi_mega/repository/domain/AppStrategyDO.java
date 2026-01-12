package com.wuin.wi_mega.repository.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.repository.base.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@TableName("app_strategy")
public class AppStrategyDO extends BaseDO {

    @Schema(description = "账户名称")
    private String name;

    /**
     * @see StrategyEnum
     */
    @Schema(description = "策略编码")
    private String code;

    @Schema(description = "基础参数")
    private String baseParam;

    @Schema(description = "运行参数")
    private String runParam;

    @Schema(description = "支持的交易对列表:['BNBUSDT', 'ETHUSDC']")
    private String symbols;

}
