package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建用户请求")
public class UserCreateReqVO {

    @Schema(description = "用户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "登录密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

}
