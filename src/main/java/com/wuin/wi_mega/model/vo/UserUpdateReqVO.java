package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新用户请求")
public class UserUpdateReqVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "用户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "登录密码")
    private String password;

    @Schema(description = "状态：1=正常，2=禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

}
