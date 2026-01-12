package com.wuin.wi_mega.model.bo.signal;

import com.alibaba.fastjson2.JSON;
import com.wuin.wi_mega.common.enums.SessionBizStatusEnum;
import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.model.bo.StrategyBizOrderParam;
import com.wuin.wi_mega.model.bo.biz.StrategyBizParam;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class SessionRunningSignalBO {

    private Long id;

    private Long accountId;

    private SymbolEnum symbol;

    private BigDecimal takeProfitPrice;

    private BigDecimal stopLossPrice;

    private StrategyEnum strategy;

    private SessionBizStatusEnum status;

    private String openPosition;

    private BigDecimal openPrice;

    @Schema(description = "补仓数据")
    private List<StrategyBizOrderParam> appends = new ArrayList<>();

    @Schema(description = "对冲数据")
    private StrategyBizOrderParam reverse = new StrategyBizOrderParam();

    public SessionRunningSignalBO(AppAccountSessionDO sessionDO) {
        this.id = sessionDO.getId();
        this.accountId = sessionDO.getAccountId();
        this.takeProfitPrice = sessionDO.getTakeProfitPrice();
        this.stopLossPrice = sessionDO.getStopLossPrice();
        this.symbol = SymbolEnum.valueOf(sessionDO.getSymbol());
        this.strategy = StrategyEnum.valueOf(sessionDO.getStrategyCode());
        this.status = SessionBizStatusEnum.byCode(sessionDO.getBizStatus());

        if (StringUtils.isNotBlank(sessionDO.getBizParam())) {
            StrategyBizParam bizParam = JSON.parseObject(sessionDO.getBizParam(), StrategyBizParam.class);
            this.openPosition = bizParam.getOpen().getPosition();
            this.appends = bizParam.getAppends();
            this.reverse = bizParam.getReverse();
        }
    }
    public StrategyBizOrderParam touchAppend(BigDecimal curPrice) {
        StrategyBizOrderParam append = this.getFirstWaitAppend();
        if (null == append) {
            return null;
        }
        if ("LONG".equals(openPosition)) {
            if (append.getPrice().compareTo(curPrice) >= 0) {
                return append;
            }
        } else if (append.getPrice().compareTo(curPrice) <= 0) {
            return append;
        }
        return null;
    }

    public StrategyBizOrderParam touchReverse(BigDecimal curPrice) {
        //必须完成所有加仓 || 加仓数据为空
        if (CollectionUtils.isNotEmpty(this.appends)) {
            for (StrategyBizOrderParam append : appends) {
                if (!append.isDone()) {
                    return null;
                }
            }
        }
        //对冲必须符合 配置存在 && 存在价格 && 存在数量
        if (null != this.reverse && this.reverse.getPrice() != null && this.reverse.getQty() != null) {
            if ("LONG".equals(openPosition) && this.reverse.getPrice().compareTo(curPrice) >= 0) {
                return this.reverse;
            }
            if ("SHORT".equals(openPosition) && this.reverse.getPrice().compareTo(curPrice) <= 0) {
                return this.reverse;
            }
        }
        return null;
    }

    private StrategyBizOrderParam getFirstWaitAppend() {
        for (StrategyBizOrderParam append : appends) {
            if (!append.isDone()) {
                return append;
            }
        }
        return null;
    }

    public boolean needTakeProfit(BigDecimal curPrice) {
        if (null == this.takeProfitPrice) {
            return false;
        }
        if ("LONG".equals(openPosition)) {
            return this.takeProfitPrice.compareTo(curPrice) <= 0;
        } else {
            return this.takeProfitPrice.compareTo(curPrice) >= 0;
        }
    }

    public boolean needStopLoss(BigDecimal curPrice) {
        if (null == this.stopLossPrice) {
            return false;
        }
        if ("LONG".equals(openPosition)) {
            return this.stopLossPrice.compareTo(curPrice) >= 0;
        } else {
            return this.stopLossPrice.compareTo(curPrice) <= 0;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SessionRunningSignalBO that = (SessionRunningSignalBO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
