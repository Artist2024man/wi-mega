package com.wuin.wi_mega.repository.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wuin.wi_mega.repository.base.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("app_user")
@Schema(description = "用户信息", name = "AppUserDO")
public class AppUserDO extends BaseDO {

    @Schema(description = "用户名称")
    private String name;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录密码")
    private String password;

    @Schema(description = "用户类型")
    private Integer userType;

    @Schema(description = "加密盐值")
    private String salt;

    /**
     * @see com.wuin.wi_mega.common.enums.UserStatusEnum
     */
    @Schema(description = "状态：1=正常，2=禁用")
    private Integer status;

    @TableField(exist = false)
    private String token;
}
