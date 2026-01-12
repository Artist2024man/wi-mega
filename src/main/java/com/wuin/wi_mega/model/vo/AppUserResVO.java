package com.wuin.wi_mega.model.vo;

import com.wuin.wi_mega.common.util.LocalLock;
import com.wuin.wi_mega.model.bo.AccountCountBO;
import com.wuin.wi_mega.model.bo.AccountEquityStatBO;
import com.wuin.wi_mega.repository.domain.AppUserDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "净值历史折线图")
public class AppUserResVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名称")
    private String name;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户类型")
    private Integer userType;

    /**
     * @see com.wuin.wi_mega.common.enums.UserStatusEnum
     */
    @Schema(description = "状态：1=正常，2=禁用")
    private Integer status;

    @Schema(description = "总账号数量")
    private Integer totalAccountNum;

    @Schema(description = "运行中账号数量")
    private Integer runningAccountNum;

    @Schema(description = "初始化净值")
    private BigDecimal initEquity;

    @Schema(description = "当前净值")
    private BigDecimal curEquity;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public AppUserResVO(AppUserDO userDO, AccountCountBO countBO, AccountEquityStatBO statBO) {
        this.id = userDO.getId();
        this.name = userDO.getName();
        this.username = userDO.getUsername();
        this.userType = userDO.getUserType();
        this.status = userDO.getStatus();
        if (null != countBO) {
            this.totalAccountNum = countBO.getTotalAccountCount();
            this.runningAccountNum = countBO.getRunningAccountCount();
        }
        if (null != statBO) {
            this.initEquity = statBO.getTotalInitEquity();
            this.curEquity = statBO.getTotalCurEquity();
        }
        this.createTime = userDO.getCreateTime();
    }
}
