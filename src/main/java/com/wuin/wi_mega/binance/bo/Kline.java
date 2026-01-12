package com.wuin.wi_mega.binance.bo;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wuin.wi_mega.common.enums.KlineIntervalEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Kline {

    private KlineIntervalEnum interval;

    private String symbol;
    //开盘时间
    private Long start;
    //关盘时间
    private Long end;
    //开盘价
    private BigDecimal open;
    //最高价
    private BigDecimal high;
    //最低价
    private BigDecimal low;
    //收盘价
    private BigDecimal close;
    //成交量（基础资产）
    private BigDecimal volume;
    //成交额（报价资产）
    private BigDecimal quoteVolume;
    //成交笔数
    private Integer tradeCount;

    public String getSide() {
        return open.compareTo(close) > 0 ? "SHORT" : "LONG";
    }

    public BigDecimal maxDistance() {
        return high.subtract(low);
    }

    public Kline() {
    }

    public Kline(JSONArray item, String symbol, KlineIntervalEnum interval) {
        this.symbol = symbol;
        this.start = Long.parseLong(item.getString(0));
        this.open = new BigDecimal(item.getString(1));
        this.high = new BigDecimal(item.getString(2));
        this.low = new BigDecimal(item.getString(3));
        this.close = new BigDecimal(item.getString(4));
        this.volume = new BigDecimal(item.getString(5));
        this.end = Long.parseLong(item.getString(6));
        this.quoteVolume = new BigDecimal(item.getString(7));
        this.tradeCount = item.getInteger(8);
        this.interval = interval;
    }

    public Kline(JSONObject item) {
        this.symbol = item.getString("s");
        this.start = Long.parseLong(item.getString("t"));
        this.open = new BigDecimal(item.getString("o"));
        this.high = new BigDecimal(item.getString("h"));
        this.low = new BigDecimal(item.getString("l"));
        this.close = new BigDecimal(item.getString("c"));
        this.end = Long.parseLong(item.getString("T"));
        this.interval = KlineIntervalEnum.byCodeStr(item.getString("i"));
        this.volume = new BigDecimal(item.getString("v"));
        this.quoteVolume = new BigDecimal(item.getString("q"));
        this.tradeCount = item.getInteger("n");
    }
}
