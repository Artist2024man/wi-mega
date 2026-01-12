package com.wuin.wi_mega.controller;

import com.wuin.wi_mega.common.annotations.AuthRequired;
import com.wuin.wi_mega.common.resp.RespModel;
import com.wuin.wi_mega.common.util.AuthUtils;
import com.wuin.wi_mega.model.vo.TradeReqVO;
import com.wuin.wi_mega.model.vo.TradeResVO;
import com.wuin.wi_mega.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/trade")
@Tag(name = "手动交易")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    // ==================== 做多相关 ====================

    @PostMapping("/openLong")
    @AuthRequired
    @Operation(summary = "开多", description = "开多仓，需要当前无多头持仓")
    public RespModel openLong(@RequestBody TradeReqVO reqVO) {
        TradeResVO result = tradeService.openLong(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success(result);
    }

    @PostMapping("/addLong")
    @AuthRequired
    @Operation(summary = "加多", description = "加多仓，需要当前有多头持仓")
    public RespModel addLong(@RequestBody TradeReqVO reqVO) {
        TradeResVO result = tradeService.addLong(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success(result);
    }

    @PostMapping("/closeLong")
    @AuthRequired
    @Operation(summary = "平多", description = "平掉多头持仓，quantity不传或传0则全部平仓")
    public RespModel closeLong(@RequestBody TradeReqVO reqVO) {
        TradeResVO result = tradeService.closeLong(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success(result);
    }

    // ==================== 做空相关 ====================

    @PostMapping("/openShort")
    @AuthRequired
    @Operation(summary = "开空", description = "开空仓，需要当前无空头持仓")
    public RespModel openShort(@RequestBody TradeReqVO reqVO) {
        TradeResVO result = tradeService.openShort(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success(result);
    }

    @PostMapping("/addShort")
    @AuthRequired
    @Operation(summary = "加空", description = "加空仓，需要当前有空头持仓")
    public RespModel addShort(@RequestBody TradeReqVO reqVO) {
        TradeResVO result = tradeService.addShort(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success(result);
    }

    @PostMapping("/closeShort")
    @AuthRequired
    @Operation(summary = "平空", description = "平掉空头持仓，quantity不传或传0则全部平仓")
    public RespModel closeShort(@RequestBody TradeReqVO reqVO) {
        TradeResVO result = tradeService.closeShort(AuthUtils.getUserLogin(), reqVO);
        return RespModel.success(result);
    }

    // ==================== 其他 ====================

    @PostMapping("/closeAll")
    @AuthRequired
    @Operation(summary = "全部平仓", description = "平掉账号所有持仓（多空都平）")
    public RespModel closeAll(@RequestBody TradeReqVO reqVO) {
        tradeService.closeAll(AuthUtils.getUserLogin(), reqVO.getAccountId());
        return RespModel.success();
    }

    @GetMapping("/position/{accountId}")
    @AuthRequired
    @Operation(summary = "查询持仓", description = "查询账号当前持仓信息")
    public RespModel getPosition(@PathVariable Long accountId) {
        return RespModel.success(tradeService.getPosition(AuthUtils.getUserLogin(), accountId));
    }
}
