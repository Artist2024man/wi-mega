package com.wuin.wi_mega.controller;

import com.wuin.wi_mega.common.annotations.AuthRequired;
import com.wuin.wi_mega.common.enums.UserTypeEnum;
import com.wuin.wi_mega.common.resp.RespModel;
import com.wuin.wi_mega.common.util.AuthUtils;
import com.wuin.wi_mega.model.PageResponseVO;
import com.wuin.wi_mega.model.vo.ExecutionLogQueryReqVO;
import com.wuin.wi_mega.model.vo.ExecutionLogVO;
import com.wuin.wi_mega.service.StrategyExecutionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 策略执行日志控制器
 */
@RestController
@Slf4j
@RequestMapping("/api/executionLog")
@Tag(name = "执行日志管理")
public class ExecutionLogController {

    @Autowired
    private StrategyExecutionLogService executionLogService;

    @PostMapping("/page")
    @AuthRequired
    @Operation(summary = "分页查询执行日志", description = "分页查询策略执行日志，支持多条件筛选")
    public RespModel pageExecutionLogs(@RequestBody(required = false) ExecutionLogQueryReqVO requestVO) {
        if (requestVO == null) {
            requestVO = new ExecutionLogQueryReqVO();
        }
        PageResponseVO<ExecutionLogVO> result = executionLogService.pageList(AuthUtils.getUserLogin().getId(), requestVO);
        return RespModel.success(result);
    }

    @PostMapping("/listBySession")
    @AuthRequired
    @Operation(summary = "根据会话ID查询日志", description = "查询指定会话的所有执行日志，按时间正序排列")
    public RespModel listLogsBySession(@RequestParam Long sessionId) {
        List<ExecutionLogVO> logs = executionLogService.listBySessionId(sessionId);
        return RespModel.success(logs);
    }

    @PostMapping("/recent")
    @AuthRequired
    @Operation(summary = "查询最近执行日志", description = "查询指定账号最近的执行日志")
    public RespModel listRecentLogs(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "50") Integer limit) {
        List<ExecutionLogVO> logs = executionLogService.listRecentByAccountId(accountId, limit);
        return RespModel.success(logs);
    }

    @GetMapping("/detail/{logId}")
    @AuthRequired
    @Operation(summary = "获取日志详情", description = "根据日志ID获取详细信息")
    public RespModel getLogDetail(@PathVariable Long logId) {
        ExecutionLogVO logVO = executionLogService.getLogDetail(logId);
        return RespModel.success(logVO);
    }

    @PostMapping("/statistics")
    @AuthRequired
    @Operation(summary = "日志统计概览", description = "获取指定账号的日志统计信息")
    public RespModel getLogStatistics(@RequestParam Long accountId) {
        Map<String, Object> stats = executionLogService.getLogStatistics(accountId);
        return RespModel.success(stats);
    }

    @PostMapping("/clean")
    @AuthRequired(userTypes = {UserTypeEnum.ADMIN})
    @Operation(summary = "清理过期日志", description = "清理指定天数之前的日志(仅超管有权限)")
    public RespModel cleanExpiredLogs(@RequestParam(defaultValue = "30") Integer days) {
        int count = executionLogService.cleanExpiredLogs(days);
        return RespModel.success(count);
    }
}
