package com.wuin.wi_mega.common.enums;

import lombok.Getter;

/**
 * 执行日志类型枚举
 */
@Getter
public enum ExecutionLogTypeEnum {

    // ========== 信号相关 ==========
    SIGNAL_GENERATED("SIGNAL_GENERATED", "信号生成", "SIGNAL", "INFO"),
    SIGNAL_IGNORED("SIGNAL_IGNORED", "信号忽略", "SIGNAL", "INFO"),
    SIGNAL_EXPIRED("SIGNAL_EXPIRED", "信号过期", "SIGNAL", "WARN"),

    // ========== 会话相关 ==========
    SESSION_CREATED("SESSION_CREATED", "会话创建", "SESSION", "INFO"),
    SESSION_CLOSED("SESSION_CLOSED", "会话关闭", "SESSION", "INFO"),

    // ========== 开仓相关 ==========
    OPEN_POSITION_START("OPEN_POSITION_START", "开仓开始", "TRADE", "INFO"),
    OPEN_POSITION_SUCCESS("OPEN_POSITION_SUCCESS", "开仓成功", "TRADE", "INFO"),
    OPEN_POSITION_FAILED("OPEN_POSITION_FAILED", "开仓失败", "TRADE", "ERROR"),
    OPEN_POSITION_IGNORED("OPEN_POSITION_IGNORED", "开仓忽略", "TRADE", "WARN"),

    // ========== 加仓相关 ==========
    APPEND_START("APPEND_START", "加仓开始", "TRADE", "INFO"),
    APPEND_SUCCESS("APPEND_SUCCESS", "加仓成功", "TRADE", "INFO"),
    APPEND_FAILED("APPEND_FAILED", "加仓失败", "TRADE", "ERROR"),
    APPEND_IGNORED("APPEND_IGNORED", "加仓忽略", "TRADE", "WARN"),

    // ========== 对冲相关 ==========
    REVERSE_START("REVERSE_START", "对冲开始", "TRADE", "INFO"),
    REVERSE_SUCCESS("REVERSE_SUCCESS", "对冲成功", "TRADE", "INFO"),
    REVERSE_FAILED("REVERSE_FAILED", "对冲失败", "TRADE", "ERROR"),

    // ========== 止盈相关 ==========
    TAKE_PROFIT_START("TAKE_PROFIT_START", "止盈开始", "TRADE", "INFO"),
    TAKE_PROFIT_SUCCESS("TAKE_PROFIT_SUCCESS", "止盈成功", "TRADE", "INFO"),
    TAKE_PROFIT_FAILED("TAKE_PROFIT_FAILED", "止盈失败", "TRADE", "ERROR"),

    // ========== 止损相关 ==========
    STOP_LOSS_START("STOP_LOSS_START", "止损开始", "TRADE", "INFO"),
    STOP_LOSS_SUCCESS("STOP_LOSS_SUCCESS", "止损成功", "TRADE", "INFO"),
    STOP_LOSS_FAILED("STOP_LOSS_FAILED", "止损失败", "TRADE", "ERROR"),

    // ========== 订单相关 ==========
    ORDER_PLACED("ORDER_PLACED", "订单提交", "ORDER", "INFO"),
    ORDER_FILLED("ORDER_FILLED", "订单成交", "ORDER", "INFO"),
    ORDER_CANCELLED("ORDER_CANCELLED", "订单取消", "ORDER", "WARN"),
    ORDER_FAILED("ORDER_FAILED", "订单失败", "ORDER", "ERROR"),

    // ========== 策略相关 ==========
    STRATEGY_START("STRATEGY_START", "策略启动", "STRATEGY", "INFO"),
    STRATEGY_STOP("STRATEGY_STOP", "策略停止", "STRATEGY", "INFO"),
    STRATEGY_ERROR("STRATEGY_ERROR", "策略异常", "STRATEGY", "ERROR"),

    // ========== 系统相关 ==========
    SYSTEM_INFO("SYSTEM_INFO", "系统信息", "SYSTEM", "INFO"),
    SYSTEM_WARN("SYSTEM_WARN", "系统警告", "SYSTEM", "WARN"),
    SYSTEM_ERROR("SYSTEM_ERROR", "系统错误", "SYSTEM", "ERROR");

    private final String code;
    private final String message;
    private final String category;  // 分类：SIGNAL, SESSION, TRADE, ORDER, STRATEGY, SYSTEM
    private final String level;     // 级别：INFO, WARN, ERROR

    ExecutionLogTypeEnum(String code, String message, String category, String level) {
        this.code = code;
        this.message = message;
        this.category = category;
        this.level = level;
    }

    public static ExecutionLogTypeEnum byCode(String code) {
        for (ExecutionLogTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}

