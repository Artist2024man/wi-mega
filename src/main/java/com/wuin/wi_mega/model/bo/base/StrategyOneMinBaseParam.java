package com.wuin.wi_mega.model.bo.base;

import com.wuin.wi_mega.model.vo.ParamKeyValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class StrategyOneMinBaseParam implements StrategyBaseParam {

    @Schema(description = "触发条件最小距离", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal mdis;

    @Schema(description = "最近两分钟最小差值", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal mldif;

    @Schema(description = "前一分钟趋势反向阈值", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal rsdis;

    @Schema(description = "触发最小间隔", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long mitl;

    @Schema(description = "最近5根K线最大涨跌幅度反转阈值", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal rdis5;

    @Schema(description = "最近5根(5m)K线最大涨跌幅度反转阈值", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal rdis55m;

    /**
     * 转换为键值对列表（带中文说明）
     */
    public List<ParamKeyValueVO> toKeyValueList() {
        List<ParamKeyValueVO> list = new ArrayList<>();
        list.add(new ParamKeyValueVO("mdis", "触发条件最小距离", mdis, null));
        list.add(new ParamKeyValueVO("mldif", "最近两分钟最小差值", mldif, null));
        list.add(new ParamKeyValueVO("rsdis", "前一分钟趋势反向阈值", rsdis, null));
        list.add(new ParamKeyValueVO("mitl", "触发最小间隔", mitl, "单位：毫秒"));
        list.add(new ParamKeyValueVO("rdis5", "最近5根K线最大涨跌幅度反转阈值", rdis5, null));
        list.add(new ParamKeyValueVO("rdis55m", "最近5根(5m)K线最大涨跌幅度反转阈值", rdis55m, null));
        return list;
    }
}
