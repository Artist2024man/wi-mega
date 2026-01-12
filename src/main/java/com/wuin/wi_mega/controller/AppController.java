package com.wuin.wi_mega.controller;

import com.wuin.wi_mega.common.annotations.AuthRequired;
import com.wuin.wi_mega.common.cache.local.KlineCacheFactory;
import com.wuin.wi_mega.common.enums.KlineIntervalEnum;
import com.wuin.wi_mega.common.enums.SessionStatusEnum;
import com.wuin.wi_mega.common.enums.SymbolEnum;
import com.wuin.wi_mega.common.resp.RespModel;
import com.wuin.wi_mega.common.util.AuthUtils;
import com.wuin.wi_mega.model.vo.AccountCreateReqVO;
import com.wuin.wi_mega.model.vo.AccountReqVO;
import com.wuin.wi_mega.model.vo.AccountStrategyUpdateReqVO;
import com.wuin.wi_mega.model.vo.AlgoOrderReqVO;
import com.wuin.wi_mega.repository.AppAccountSessionRepository;
import com.wuin.wi_mega.service.AppAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@Slf4j
@RequestMapping("/api/app")
@Tag(name = "用户端专用")
public class AppController {

    @Autowired
    private AppAccountService appAccountService;

    @Autowired
    private AppAccountSessionRepository appAccountSessionRepository;

    @Autowired
    private KlineCacheFactory klineCacheFactory;

    @GetMapping("/positionRisk/{accountId}")
    @Operation(summary = "账户持仓信息", description = "账户持仓信息")
    @AuthRequired
    public RespModel balance(@PathVariable Long accountId) {
        return RespModel.success(appAccountService.positionRisk(AuthUtils.getUserLogin(), accountId));
    }

    @PostMapping("/create/account")
    @AuthRequired
    @Operation(summary = "创建账号", description = "使用该接口将账号纳管到平台")
    public RespModel create(@RequestBody AccountCreateReqVO reqVO) {
        return RespModel.success(appAccountService.create(AuthUtils.getUserLogin(), reqVO));
    }

    @GetMapping("/accountInfo/{accountId}")
    @Operation(summary = "账号信息", description = "账号信息")
    @AuthRequired
    public RespModel accountInfo(@PathVariable Long accountId) {
        return RespModel.success(appAccountService.accountInfo(AuthUtils.getUserLogin(), accountId));
    }

    @GetMapping("/strategy/list/{accountId}")
    @Operation(summary = "策略列表", description = "策略列表")
    @AuthRequired
    public RespModel strategyList4Acc(@PathVariable Long accountId) {
        return RespModel.success(appAccountService.strategyList(AuthUtils.getUserLogin(), accountId));
    }

    @GetMapping("/strategy/list")
    @Operation(summary = "策略列表", description = "策略列表")
    @AuthRequired
    public RespModel strategyList() {
        return RespModel.success(appAccountService.strategyList(AuthUtils.getUserLogin(), null));
    }

    @GetMapping("/startStrategy/{accountId}")
    @Operation(summary = "开始策略", description = "开始账号策略，账号必须配置有有效的策略才可以开始，开始后请勿在其他地方操作账号")
    @AuthRequired
    public RespModel start(@PathVariable Long accountId, BigDecimal minStrategyPrice, BigDecimal maxStrategyPrice) {
        appAccountService.start(AuthUtils.getUserLogin(), new AccountReqVO(accountId, minStrategyPrice, maxStrategyPrice));
        return RespModel.success();
    }

    @GetMapping("/stopStrategy/{accountId}")
    @Operation(summary = "停止账号策略", description = "停止策略，停止后系统将直接停止对账号的任何操作")
    @AuthRequired
    public RespModel stop(@PathVariable Long accountId) {
        appAccountService.stop(AuthUtils.getUserLogin(), new AccountReqVO(accountId));
        return RespModel.success();
    }

    @GetMapping("/symbol/price/{symbol}")
    @Operation(summary = "交易对价格", description = "交易对价格")
    public RespModel symbolPrice(@PathVariable String symbol) {
        return RespModel.success(klineCacheFactory.getCurPrice(SymbolEnum.valueOf(symbol), KlineIntervalEnum.MINUTE_1));
    }

    @GetMapping("/runningSession/{accountId}")
    @Operation(summary = "运行中的会话", description = "运行中的会话")
    @AuthRequired
    public RespModel runningSession(@PathVariable Long accountId) {
        return RespModel.success(appAccountService.runningSession(AuthUtils.getUserLogin(), accountId));
    }

    @GetMapping("/historyList/{accountId}")
    @Operation(summary = "历史会话", description = "历史会话")
    @AuthRequired
    public RespModel historyList(@PathVariable Long accountId) {
        return RespModel.success(appAccountSessionRepository.listByAccountIdAndStatusListNoCache(accountId, SessionStatusEnum.completed()));
    }

    @GetMapping("/close/{accountId}/{position}")
    @Operation(summary = "平仓", description = "平仓")
    @AuthRequired
    public RespModel close(@PathVariable Long accountId, @PathVariable String position, Long sessionId) {
        appAccountService.stopSession(AuthUtils.getUserLogin(), accountId, position, sessionId);
        return RespModel.success();
    }

    @GetMapping("/open/{accountId}/{position}")
    @Operation(summary = "开仓", description = "开仓")
    @AuthRequired
    public RespModel open(@PathVariable Long accountId, @PathVariable String position, BigDecimal amount, Long sessionId) {
        appAccountService.open(AuthUtils.getUserLogin(), accountId, position, amount, sessionId);
        return RespModel.success();
    }

    @PostMapping("/algo/order")
    @Operation(summary = "条件单下单", description = "条件单下单")
    @AuthRequired
    public RespModel algoOrder(@RequestBody AlgoOrderReqVO reqVO) {
        appAccountService.algoOrder(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success();
    }

    @PostMapping("/update/strategy")
    @AuthRequired
    @Operation(summary = "修改策略信息", description = "修改策略信息")
    public RespModel updateStrategy(@RequestBody AccountStrategyUpdateReqVO reqVO) {
        appAccountService.updateStrategy(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success();
    }


}
