package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新用户请求")
public class UserChangePwdReqVO {

    @Schema(description = "旧密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldPwd;

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPwd;

    @Schema(description = "密码确认", requiredMode = Schema.RequiredMode.REQUIRED)
    private String checkPwd;

}
