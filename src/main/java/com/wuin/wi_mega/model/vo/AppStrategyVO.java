package com.wuin.wi_mega.model.vo;

import com.alibaba.fastjson2.JSON;
import com.wuin.wi_mega.repository.domain.AppStrategyDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Data
@Slf4j
@NoArgsConstructor
public class AppStrategyVO {

    @Schema(description = "策略ID")
    private Long id;

    @Schema(description = "策略名称")
    private String name;

    @Schema(description = "策略编码")
    private String code;

    @Schema(description = "支持的交易对列表")
    private List<String> symbols;

    @Schema(description = "基础参数默认值")
    private Map<String, Object> baseParamDefaults;

    @Schema(description = "运行参数默认值")
    private Map<String, Object> runParamDefaults;

    @Schema(description = "基础参数元数据（字段描述、类型、校验规则）")
    private List<StrategyParamMetaVO> baseParamMeta;

    @Schema(description = "运行参数元数据（字段描述、类型、校验规则）")
    private List<StrategyParamMetaVO> runParamMeta;

    /**
     * 基础构造（不含元数据）
     */
    public AppStrategyVO(AppStrategyDO strategyDO) {
        this.id = strategyDO.getId();
        this.name = strategyDO.getName();
        this.code = strategyDO.getCode();

        // 解析支持的交易对
        try {
            this.symbols = JSON.parseArray(strategyDO.getSymbols(), String.class);
        } catch (Exception e) {
            log.warn("解析symbols失败: {}", strategyDO.getSymbols());
            this.symbols = List.of();
        }

        // 解析默认参数值
        try {
            this.baseParamDefaults = JSON.parseObject(strategyDO.getBaseParam(), Map.class);
        } catch (Exception e) {
            log.warn("解析baseParam失败: {}", strategyDO.getBaseParam());
        }

        try {
            this.runParamDefaults = JSON.parseObject(strategyDO.getRunParam(), Map.class);
        } catch (Exception e) {
            log.warn("解析runParam失败: {}", strategyDO.getRunParam());
        }
    }

    /**
     * 设置参数元数据
     */
    public void setParamMeta(List<StrategyParamMetaVO> baseParamMeta, List<StrategyParamMetaVO> runParamMeta) {
        this.baseParamMeta = baseParamMeta;
        this.runParamMeta = runParamMeta;
    }
}
