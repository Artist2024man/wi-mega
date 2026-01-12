package com.wuin.wi_mega.model.vo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.wuin.wi_mega.common.enums.StrategyEnum;
import com.wuin.wi_mega.common.enums.StrategyInstanceStatusEnum;
import com.wuin.wi_mega.repository.domain.AppStrategyInstanceDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@NoArgsConstructor
public class AppStrategyInstanceVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "所属策略ID")
    private Long strategyId;

    @Schema(description = "策略名称")
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

    @Schema(description = "基础参数（动态JSON）")
    private Map<String, Object> baseParam;

    @Schema(description = "运行参数（动态JSON）")
    private Map<String, Object> runParam;

    @Schema(description = "基础参数(键值对格式，带中文标签)")
    private List<ParamKeyValueVO> baseParamList;

    @Schema(description = "运行参数(键值对格式，带中文标签)")
    private List<ParamKeyValueVO> runParamList;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "上下架状态：1=已上架，2=已下架")
    private Integer status;

    @Schema(description = "上下架状态名称")
    private String statusName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    public AppStrategyInstanceVO(AppStrategyInstanceDO instanceDO) {
        this.id = instanceDO.getId();
        this.strategyId = instanceDO.getStrategyId();
        this.name = instanceDO.getName();
        this.code = instanceDO.getCode();
        this.exchange = instanceDO.getExchange();
        this.symbol = instanceDO.getSymbol();
        this.remark = instanceDO.getRemark();
        this.status = instanceDO.getStatus();
        this.statusName = StrategyInstanceStatusEnum.getNameByCode(instanceDO.getStatus());
        this.createTime = instanceDO.getCreateTime();
        this.updateTime = instanceDO.getUpdateTime();

        // 动态解析参数为Map
        try {
            this.baseParam = JSON.parseObject(instanceDO.getBaseParam(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析baseParam失败, instanceId={}, baseParam={}", instanceDO.getId(), instanceDO.getBaseParam());
        }

        try {
            this.runParam = JSON.parseObject(instanceDO.getRunParam(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析runParam失败, instanceId={}, runParam={}", instanceDO.getId(), instanceDO.getRunParam());
        }
    }
}
