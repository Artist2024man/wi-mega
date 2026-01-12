package com.wuin.wi_mega.controller;

import com.wuin.wi_mega.binance.bo.BinancePosition;
import com.wuin.wi_mega.common.annotations.AuthRequired;
import com.wuin.wi_mega.common.resp.RespModel;
import com.wuin.wi_mega.common.util.AuthUtils;
import com.wuin.wi_mega.model.PageRequestVO;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.vo.*;
import com.wuin.wi_mega.service.AppAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/account")
@Tag(name = "账号管理")
public class AccountController {

    @Autowired
    private AppAccountService appAccountService;

    @PostMapping("/create")
    @AuthRequired
    @Operation(summary = "创建账号", description = "使用该接口将账号纳管到平台")
    public RespModel create(@RequestBody AccountCreateReqVO reqVO) {
        return RespModel.success(appAccountService.create(AuthUtils.getUserLogin(), reqVO));
    }

    @PostMapping("/delete/{id}")
    @AuthRequired
    @Operation(summary = "删除账号", description = "从平台中删除某账号，必须先停止策略才可以删除")
    public RespModel delete(@PathVariable Long id) {
        appAccountService.delete(AuthUtils.getUserLogin(), id);
        return RespModel.success();
    }

    @PostMapping("/pageList")
    @AuthRequired
    @Operation(summary = "获取账号列表", description = "获取当前登录用户下的所有账号信息")
    public PageResponseVO<AccountResVO> pageList(@RequestBody PageRequestVO requestVO) {
        return appAccountService.pageList(AuthUtils.getUserLogin(), requestVO);
    }

    @PostMapping("/update")
    @AuthRequired
    @Operation(summary = "更新账号信息", description = "修改账号的名称、AK、SK、策略等信息")
    public RespModel update(@RequestBody AccountUpdateReqVO reqVO) {
        appAccountService.update(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success();
    }

    @PostMapping("/startStrategy")
    @AuthRequired
    @Operation(summary = "开始策略", description = "开始账号策略，账号必须配置有有效的策略才可以开始，开始后请勿在其他地方操作账号")
    public RespModel start(@RequestBody AccountReqVO reqVO) {
        appAccountService.start(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success();
    }

    @PostMapping("/stopStrategy")
    @AuthRequired
    @Operation(summary = "停止账号策略", description = "停止策略，停止后系统将直接停止对账号的任何操作")
    public RespModel stop(@RequestBody AccountReqVO reqVO) {
        appAccountService.stop(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success();
    }

    @PostMapping("/positionRisk")
    @AuthRequired
    @Operation(
            summary = "账户持仓信息",
            description = "查询用户当前持仓信息，包含止盈止损、加仓信息、反仓价位等策略业务数据",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PositionRiskResponse.class)
                            )
                    )
            }
    )
    public RespModel positionRisk(Long accountId) {
        return RespModel.success(appAccountService.positionRiskDetail(AuthUtils.getUserLogin(), accountId));
    }

    @PostMapping("/history/line")
    @AuthRequired
    @Operation(summary = "账号历史净值", description = "返回账号近一段时间的净值历史，折线图的形式展示")
    public HistoryLineVO historyLine(@RequestBody AccountHistoryLineReqVO reqVO) {
        return appAccountService.historyLine(AuthUtils.getUserLogin(), reqVO);
    }

    @PostMapping("/session/current")
    @AuthRequired
    @Operation(summary = "获取当前运行会话详情", description = "获取账号当前正在运行的交易会话详情，包含持仓、止盈止损、未实现盈亏等信息")
    public RespModel getCurrentSession(@RequestParam Long accountId) {
        SessionDetailVO result = appAccountService.getCurrentSession(AuthUtils.getUserLogin(), accountId);
        return RespModel.success(result);
    }

    @PostMapping("/session/history")
    @AuthRequired
    @Operation(summary = "分页查询会话历史", description = "分页查询账号的历史交易会话，可按状态、时间范围筛选")
    public PageResponseVO<SessionHistoryVO> getSessionHistory(@RequestBody PageRequestVO<SessionHistoryReqVO> requestVO) {
        return appAccountService.getSessionHistory(AuthUtils.getUserLogin(), requestVO);
    }

    @PostMapping("/order/history")
    @AuthRequired
    @Operation(summary = "分页查询订单历史", description = "分页查询账号的历史订单记录，可按订单类型、状态、时间范围等筛选")
    public PageResponseVO<OrderHistoryVO> getOrderHistory(@RequestBody PageRequestVO<OrderHistoryReqVO> requestVO) {
        return appAccountService.getOrderHistory(AuthUtils.getUserLogin(), requestVO);
    }

    @PostMapping("/strategy/status")
    @AuthRequired
    @Operation(summary = "获取策略运行状态汇总", description = "获取账号的策略运行统计数据，包括胜率、盈亏比、今日/累计盈亏等")
    public RespModel getStrategyStatus(@RequestParam Long accountId) {
        StrategyStatusVO result = appAccountService.getStrategyStatus(AuthUtils.getUserLogin(), accountId);
        return RespModel.success(result);
    }
}
