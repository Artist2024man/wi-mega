package com.wuin.wi_mega.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "查询用户列表请求")
public class AppUserListReqVO {

    @Schema(description = "用户名称")
    private String name;

    @Schema(description = "用户名")
    private String username;

    /**
     * @see com.wuin.wi_mega.common.enums.UserStatusEnum
     */
    @Schema(description = "状态：1=正常，2=禁用")
    private Integer status;

}
