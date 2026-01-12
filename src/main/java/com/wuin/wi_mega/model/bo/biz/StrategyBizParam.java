package com.wuin.wi_mega.model.bo.biz;

import com.wuin.wi_mega.model.bo.StrategyBizOrderParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 一分钟超短线业务参数
 */
@Data
public class StrategyBizParam {
    @Schema(description = "开仓数据")
    private StrategyBizOrderParam open = new StrategyBizOrderParam();
    @Schema(description = "补仓数据")
    private List<StrategyBizOrderParam> appends = new ArrayList<>();
    @Schema(description = "对冲数据")
    private StrategyBizOrderParam reverse;

    public StrategyBizOrderParam getAppend(int idx) {
        for (StrategyBizOrderParam append : appends) {
            if (append.getIdx() == idx) {
                return append;
            }
        }
        return null;
    }
}
