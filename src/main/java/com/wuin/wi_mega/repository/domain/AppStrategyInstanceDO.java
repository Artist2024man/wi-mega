package com.wuin.wi_mega.repository.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.common.enums.StrategyInstanceStatusEnum;
import com.wuin.wi_mega.repository.base.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@TableName("app_strategy_instance")
public class AppStrategyInstanceDO extends BaseDO {

    @Schema(description = "所属策略ID")
    private Long strategyId;

    @Schema(description = "账户名称")
    private String name;
    /**
     * @see StrategyEnum
     */
    @Schema(description = "策略编码")
    private String code;

    @Schema(description = "交易平台")
    private String exchange;

    @Schema(description = "交易对")
    private String symbol;

    @Schema(description = "基础参数")
    private String baseParam;

    @Schema(description = "运行参数")
    private String runParam;

    @Schema(description = "备注说明")
    private String remark;

    /**
     * @see StrategyInstanceStatusEnum
     */
    @Schema(description = "上下架状态：1=已上架，2=已下架")
    private Integer status;
}
