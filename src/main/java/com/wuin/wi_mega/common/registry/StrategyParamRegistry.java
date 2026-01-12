package com.wuin.wi_mega.common.registry;

import com.wuin.wi_mega.common.enums.KlineIntervalEnum;
import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.model.bo.base.StrategyBaseParam;
import com.wuin.wi_mega.model.bo.base.StrategyMartingaleBaseParam;
import com.wuin.wi_mega.model.bo.base.StrategyOneMinBaseParam;
import com.wuin.wi_mega.model.bo.running.StrategyMartingaleRunParam;
import com.wuin.wi_mega.model.bo.running.StrategyOneMinRunParam;
import com.wuin.wi_mega.model.vo.StrategyParamMetaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import com.wuin.wi_mega.model.vo.ParamKeyValueVO;

import java.math.BigDecimal;
import java.util.*;

/**
 * 策略参数注册中心
 */
@Component
@Slf4j
public class StrategyParamRegistry {

    /**
     * 获取基础参数元数据
     */
    public List<StrategyParamMetaVO> getBaseParamMeta(String strategyCode) {
        StrategyEnum strategyEnum = getStrategyEnum(strategyCode);
        if (strategyEnum == null) {
            return Collections.emptyList();
        }

        switch (strategyEnum) {
            case MARTINGALE_APPEND:
            case MARTINGALE_SIGNAL:
                return buildMartingaleBaseMeta();
            case ONE_MIN_SHORT:
                return buildOneMinShortBaseMeta();
            default:
                return Collections.emptyList();
        }
    }

    /**
     * 获取运行参数元数据
     */
    public List<StrategyParamMetaVO> getRunParamMeta(String strategyCode) {
        StrategyEnum strategyEnum = getStrategyEnum(strategyCode);
        if (strategyEnum == null) {
            return Collections.emptyList();
        }

        switch (strategyEnum) {
            case MARTINGALE_APPEND:
            case MARTINGALE_SIGNAL:
                return buildMartingaleRunMeta();
            case ONE_MIN_SHORT:
                return buildOneMinShortRunMeta();
            default:
                return Collections.emptyList();
        }
    }

    /**
     * 获取基础参数默认值
     */
    public Map<String, Object> getBaseParamDefaults(String strategyCode) {
        StrategyEnum strategyEnum = getStrategyEnum(strategyCode);
        if (strategyEnum == null) {
            return Collections.emptyMap();
        }

        switch (strategyEnum) {
            case MARTINGALE_APPEND:
            case MARTINGALE_SIGNAL:
                return buildMartingaleBaseDefaults();
            case ONE_MIN_SHORT:
                return buildOneMinShortBaseDefaults();
            default:
                return Collections.emptyMap();
        }
    }

    /**
     * 获取运行参数默认值
     */
    public Map<String, Object> getRunParamDefaults(String strategyCode) {
        StrategyEnum strategyEnum = getStrategyEnum(strategyCode);
        if (strategyEnum == null) {
            return Collections.emptyMap();
        }

        switch (strategyEnum) {
            case MARTINGALE_APPEND:
            case MARTINGALE_SIGNAL:
                return buildMartingaleRunDefaults();
            case ONE_MIN_SHORT:
                return buildOneMinShortRunDefaults();
            default:
                return Collections.emptyMap();
        }
    }

    /**
     * 获取基础参数Class
     */
    public Class<? extends StrategyBaseParam> getBaseParamClass(String strategyCode) {
        StrategyEnum strategyEnum = getStrategyEnum(strategyCode);
        if (strategyEnum == null) {
            return null;
        }

        switch (strategyEnum) {
            case MARTINGALE_APPEND:
            case MARTINGALE_SIGNAL:
                return StrategyMartingaleBaseParam.class;
            case ONE_MIN_SHORT:
                return StrategyOneMinBaseParam.class;
            default:
                return null;
        }
    }

    private StrategyEnum getStrategyEnum(String code) {
        if (code == null) {
            return null;
        }
        for (StrategyEnum e : StrategyEnum.values()) {
            if (e.name().equals(code)) {
                return e;
            }
        }
        return null;
    }

    // ==================== 马丁策略参数 ====================

    private List<StrategyParamMetaVO> buildMartingaleBaseMeta() {
        List<StrategyParamMetaVO> list = new ArrayList<>();

        list.add(StrategyParamMetaVO.builder()
                .field("interval").label("K线周期").type("ENUM")
                .defaultValue("MINUTE_1").required(true)
                .description("使用的K线周期")
                .options(Arrays.stream(KlineIntervalEnum.values()).map(Enum::name).toArray())
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("rsiPeriod").label("RSI周期").type("INTEGER")
                .defaultValue(4).required(true).min(2).max(50)
                .description("RSI指标计算周期")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("rsiOversold").label("RSI超卖线").type("DECIMAL")
                .defaultValue(25.0).required(true).min(0.0).max(50.0)
                .description("RSI低于此值视为超卖")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("rsiOverbought").label("RSI超买线").type("DECIMAL")
                .defaultValue(75.0).required(true).min(50.0).max(100.0)
                .description("RSI高于此值视为超买")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("emaFast").label("EMA快线周期").type("INTEGER")
                .defaultValue(3).required(true).min(1).max(50)
                .description("快速EMA周期")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("emaSlow").label("EMA慢线周期").type("INTEGER")
                .defaultValue(7).required(true).min(1).max(100)
                .description("慢速EMA周期")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("volumePeriod").label("成交量周期").type("INTEGER")
                .defaultValue(3).required(true).min(1).max(50)
                .description("成交量均值计算周期")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("volumeThreshold").label("成交量阈值").type("DECIMAL")
                .defaultValue(1.2).required(true).min(0.5).max(10.0).step(0.1)
                .description("当前成交量/均值的倍数阈值")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("minScore").label("最小评分").type("DECIMAL")
                .defaultValue(39.0).required(true).min(0.0).max(100.0)
                .description("信号触发的最低评分")
                .build());

        return list;
    }

    private List<StrategyParamMetaVO> buildMartingaleRunMeta() {
        List<StrategyParamMetaVO> list = new ArrayList<>();

        list.add(StrategyParamMetaVO.builder()
                .field("opqty").label("开仓数量").type("DECIMAL")
                .defaultValue(0.01).required(true).min(0.001).step(0.001)
                .description("每次开仓的数量").unit("个")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("mintp").label("最小止盈").type("DECIMAL")
                .defaultValue(1.0).required(true).min(0.0).step(0.1)
                .description("最小止盈金额").unit("USDT")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("sl").label("止损金额").type("DECIMAL")
                .defaultValue(5.0).required(true).min(0.0).step(0.1)
                .description("单笔止损金额").unit("USDT")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("aplos1").label("第一次补仓亏损").type("DECIMAL")
                .defaultValue(2.0).required(true).min(0.0).step(0.1)
                .description("触发第一次补仓的亏损金额").unit("USDT")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("apqty1").label("第一次补仓数量").type("DECIMAL")
                .defaultValue(0.02).required(true).min(0.001).step(0.001)
                .description("第一次补仓的数量").unit("个")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("aplos2").label("第二次补仓亏损").type("DECIMAL")
                .defaultValue(4.0).required(true).min(0.0).step(0.1)
                .description("触发第二次补仓的亏损金额").unit("USDT")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("apqty2").label("第二次补仓数量").type("DECIMAL")
                .defaultValue(0.04).required(true).min(0.001).step(0.001)
                .description("第二次补仓的数量").unit("个")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("rslos").label("对冲触发亏损").type("DECIMAL")
                .defaultValue(10.0).required(false).min(0.0).step(0.1)
                .description("触发对冲的亏损金额，不填则不对冲").unit("USDT")
                .build());

        return list;
    }

    private Map<String, Object> buildMartingaleBaseDefaults() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("interval", "MINUTE_1");
        map.put("rsiPeriod", 4);
        map.put("rsiOversold", 25.0);
        map.put("rsiOverbought", 75.0);
        map.put("emaFast", 3);
        map.put("emaSlow", 7);
        map.put("volumePeriod", 3);
        map.put("volumeThreshold", 1.2);
        map.put("minScore", 39.0);
        return map;
    }

    private Map<String, Object> buildMartingaleRunDefaults() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("opqty", new BigDecimal("0.01"));
        map.put("mintp", new BigDecimal("1.0"));
        map.put("sl", new BigDecimal("5.0"));
        map.put("aplos1", new BigDecimal("2.0"));
        map.put("apqty1", new BigDecimal("0.02"));
        map.put("aplos2", new BigDecimal("4.0"));
        map.put("apqty2", new BigDecimal("0.04"));
        map.put("rslos", new BigDecimal("10.0"));
        return map;
    }

    // ==================== 一分钟超短线策略参数 ====================

    private List<StrategyParamMetaVO> buildOneMinShortBaseMeta() {
        List<StrategyParamMetaVO> list = new ArrayList<>();

        list.add(StrategyParamMetaVO.builder()
                .field("mdis").label("触发条件最小距离").type("DECIMAL")
                .defaultValue(1.0).required(true).min(0.0).step(0.1)
                .description("价格波动触发的最小距离").unit("USDT")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("mldif").label("最近两分钟最小差值").type("DECIMAL")
                .defaultValue(0.5).required(true).min(0.0).step(0.1)
                .description("最近两分钟K线的最小价差").unit("USDT")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("rsdis").label("反向阈值").type("DECIMAL")
                .defaultValue(0.3).required(true).min(0.0).step(0.1)
                .description("前一分钟趋势反向阈值").unit("USDT")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("mitl").label("触发最小间隔").type("INTEGER")
                .defaultValue(60000).required(true).min(1000).step(1000)
                .description("两次触发的最小时间间隔").unit("毫秒")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("rdis5").label("5根K线反转阈值").type("DECIMAL")
                .defaultValue(2.0).required(true).min(0.0).step(0.1)
                .description("最近5根1分钟K线最大涨跌幅反转阈值").unit("USDT")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("rdis55m").label("5根5分钟K线反转阈值").type("DECIMAL")
                .defaultValue(5.0).required(true).min(0.0).step(0.1)
                .description("最近5根5分钟K线最大涨跌幅反转阈值").unit("USDT")
                .build());

        return list;
    }

    private List<StrategyParamMetaVO> buildOneMinShortRunMeta() {
        List<StrategyParamMetaVO> list = new ArrayList<>();

        list.add(StrategyParamMetaVO.builder()
                .field("opqty").label("开仓数量").type("DECIMAL")
                .defaultValue(0.01).required(true).min(0.001).step(0.001)
                .description("每次开仓的数量").unit("个")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("tpRatio").label("止盈比例").type("DECIMAL")
                .defaultValue(0.003).required(true).min(0.001).max(0.1).step(0.001)
                .description("止盈价格比例")
                .build());

        list.add(StrategyParamMetaVO.builder()
                .field("slRatio").label("止损比例").type("DECIMAL")
                .defaultValue(0.005).required(true).min(0.001).max(0.1).step(0.001)
                .description("止损价格比例")
                .build());

        return list;
    }

    private Map<String, Object> buildOneMinShortBaseDefaults() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("mdis", new BigDecimal("1.0"));
        map.put("mldif", new BigDecimal("0.5"));
        map.put("rsdis", new BigDecimal("0.3"));
        map.put("mitl", 60000L);
        map.put("rdis5", new BigDecimal("2.0"));
        map.put("rdis55m", new BigDecimal("5.0"));
        return map;
    }

    private Map<String, Object> buildOneMinShortRunDefaults() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("opqty", new BigDecimal("0.01"));
        map.put("tpRatio", new BigDecimal("0.003"));
        map.put("slRatio", new BigDecimal("0.005"));
        return map;
    }

    /**
     * 将参数值转换为键值对列表（带中文标签）
     */
    public List<ParamKeyValueVO> toParamKeyValueList(String strategyCode, Map<String, Object> paramValues, boolean isBaseParam) {
        List<StrategyParamMetaVO> metaList = isBaseParam
                ? getBaseParamMeta(strategyCode)
                : getRunParamMeta(strategyCode);

        if (metaList == null || metaList.isEmpty() || paramValues == null) {
            return new ArrayList<>();
        }

        List<ParamKeyValueVO> result = new ArrayList<>();
        for (StrategyParamMetaVO meta : metaList) {
            Object value = paramValues.get(meta.getField());
            result.add(new ParamKeyValueVO(
                    meta.getField(),
                    meta.getLabel(),
                    value,
                    meta.getUnit()
            ));
        }
        return result;
    }

}
