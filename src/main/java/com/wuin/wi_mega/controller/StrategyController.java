package com.wuin.wi_mega.controller;

import com.wuin.wi_mega.common.annotations.AuthRequired;
import com.wuin.wi_mega.common.enums.UserTypeEnum;
import com.wuin.wi_mega.common.registry.StrategyParamRegistry;
import com.wuin.wi_mega.common.resp.RespModel;
import com.wuin.wi_mega.common.util.AuthUtils;
import com.wuin.wi_mega.model.PageRequestVO;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.vo.*;
import com.wuin.wi_mega.service.StrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/strategy")
@Tag(name = "策略管理")
public class StrategyController {

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private StrategyParamRegistry strategyParamRegistry;

    @PostMapping("/list")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    @Operation(summary = "策略模板列表", description = "返回系统支持的模板列表，创建策略时的下拉框使用该接口，选择后需要填充对应的模板参数信息(仅超管有权限)")
    public RespModel strategyList() {
        return RespModel.success(strategyService.listStrategy(AuthUtils.getUserLogin()));
    }

    @PostMapping("/instance/list")
    @Operation(summary = "策略市场列表（用户端）", description = "返回已上架的策略列表，用户在策略市场选择策略")
    public PageResponseVO<AppStrategyInstanceVO> listInstance(@RequestBody(required = false) PageRequestVO<AppStrategyInstanceListReqVO> requestVO) {
        if (requestVO == null) {
            requestVO = new PageRequestVO<>();
        }
        return strategyService.listInstance(AuthUtils.getUserLogin(), requestVO);
    }

    @PostMapping("/instance/listAll")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    @Operation(summary = "策略管理列表（管理端）", description = "返回所有策略列表，包括已上架和已下架的(仅超管有权限)")
    public PageResponseVO<AppStrategyInstanceVO> listInstanceForAdmin(@RequestBody(required = false) PageRequestVO<AppStrategyInstanceListReqVO> requestVO) {
        if (requestVO == null) {
            requestVO = new PageRequestVO<>();
        }
        return strategyService.listInstanceForAdmin(AuthUtils.getUserLogin(), requestVO);
    }

    @PostMapping("/instance/create")
    @Operation(summary = "创建策略", description = "创建一个策略(仅超管有权限)")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    public RespModel createInstance(@RequestBody AppStrategyInstanceCreateReqVO requestVO) {
        Long instanceId = strategyService.createInstance(AuthUtils.getUserLogin(), requestVO);
        return RespModel.success(instanceId);
    }

    @PostMapping("/instance/update")
    @Operation(summary = "修改策略信息", description = "更新策略信息、修改策略参数、上下架策略(仅超管有权限)")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    public RespModel updateInstance(@RequestBody AppStrategyInstanceCreateReqVO requestVO) {
        strategyService.updateInstance(AuthUtils.getUserLogin(), requestVO);
        return RespModel.success();
    }

    @PostMapping("/instance/delete")
    @Operation(summary = "删除策略", description = "需要先停止所有使用该策略的账号(仅超管有权限)")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    public RespModel deleteInstance(@RequestBody AppStrategyInstanceDeleteReqVO requestVO) {
        strategyService.deleteInstance(AuthUtils.getUserLogin(), requestVO);
        return RespModel.success();
    }

    @PostMapping("/instance/updateStatus")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    @Operation(summary = "策略上下架", description = "修改策略的上下架状态(仅超管有权限)")
    public RespModel updateStatus(@RequestBody AppStrategyInstanceStatusReqVO requestVO) {
        strategyService.updateStatus(AuthUtils.getUserLogin(), requestVO);
        return RespModel.success();
    }
}
