package com.wuin.wi_mega.controller;

import com.wuin.wi_mega.common.annotations.AuthRequired;
import com.wuin.wi_mega.common.enums.UserTypeEnum;
import com.wuin.wi_mega.common.resp.RespModel;
import com.wuin.wi_mega.common.util.AuthUtils;
import com.wuin.wi_mega.model.PageRequestVO;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.vo.*;
import com.wuin.wi_mega.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/user")
@Tag(name = "用户管理")
public class UserController {

    @Autowired
    private AppUserService appUserService;

    @PostMapping("/list")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    @Operation(summary = "获取用户列表",description = "仅超管有权限")
    public PageResponseVO<AppUserResVO> list(@RequestBody PageRequestVO<AppUserListReqVO> requestVO) {
        return appUserService.list(AuthUtils.getUserLogin(), requestVO);
    }

    @PostMapping("/create")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    @Operation(summary = "创建用户",description = "仅超管有权限")
    public RespModel create(@RequestBody UserCreateReqVO reqVO) {
        return RespModel.success(appUserService.create(AuthUtils.getUserLogin(), reqVO));
    }

    @PostMapping("/update")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    @Operation(summary = "修改用户信息",description = "仅超管有权限")
    public RespModel update(@RequestBody UserUpdateReqVO reqVO) {
        appUserService.update(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success();
    }

    @PostMapping("/delete/{id}")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    @Operation(summary = "删除用户", description = "仅超管有权限，用户名下有绑定账号时不能删除")
    public RespModel delete(@PathVariable Long id) {
        appUserService.delete(AuthUtils.getUserLogin(), id);
        return RespModel.success();
    }

    @PostMapping("/changePwd")
    @AuthRequired
    @Operation(summary = "修改用户密码",description = "用户自己修改自己的密码")
    public RespModel changePwd(@RequestBody UserChangePwdReqVO reqVO) {
        appUserService.changePwd(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success();
    }

    @PostMapping("/history/line")
    @AuthRequired
    @Operation(summary = "获取用户净值历史",description = "历史用户净值，采用折线图的形式展示")
    public HistoryLineVO historyLine(@RequestBody UserHistoryLineReqVO reqVO) {
        return appUserService.historyLine(AuthUtils.getUserLogin(), reqVO);
    }

    @PostMapping("/info")
    @AuthRequired
    @Operation(summary = "当前用户信息",description = "获取当前登录的用户的基础信息")
    public RespModel info() {
        return RespModel.success(appUserService.getUserInfo(AuthUtils.getUserLogin()));
    }

    @PostMapping("/login")
    @Operation(summary = "登录",description = "登录系统，返回token")
    public RespModel login(@RequestBody UserLoginReqVO reqVO) {
        return RespModel.success(appUserService.login(reqVO));
    }

    @PostMapping("/logout")
    @AuthRequired
    @Operation(summary = "登出",description = "退出当前登录的账号")
    public RespModel logout() {
        appUserService.logout(AuthUtils.getUserLogin());
        return RespModel.success();
    }

}
