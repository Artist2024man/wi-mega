/*
 Navicat Premium Data Transfer

 Source Server         : wi-mega
 Source Server Type    : MySQL
 Source Server Version : 80407 (8.4.7)
 Source Host           : 127.0.0.1:3306
 Source Schema         : demo

 Target Server Type    : MySQL
 Target Server Version : 80407 (8.4.7)
 File Encoding         : 65001

 Date: 12/01/2026 11:04:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_account
-- ----------------------------
DROP TABLE IF EXISTS `app_account`;
CREATE TABLE `app_account` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                               `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '名称',
                               `user_id` bigint NOT NULL COMMENT '键',
                               `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易所,目前：BIANCE',
                               `api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'api key',
                               `api_key_pass` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'api key pass',
                               `init_equity` decimal(20,8) NOT NULL COMMENT '初始净值',
                               `cur_equity` decimal(20,8) NOT NULL COMMENT '当前净值',
                               `equity_coin` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '净值单位，当前支持：USDT,USD,USDC,USD1',
                               `symbol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '交易对',
                               `strategy_instance_id` bigint DEFAULT NULL COMMENT '策略实例ID',
                               `strategy_status` tinyint NOT NULL COMMENT '策略状态：0=停止，1=运行中',
                               `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注说明',
                               `next_sync_time` datetime DEFAULT NULL COMMENT '下次同步时间',
                               `dual_side_position` int DEFAULT NULL COMMENT '持仓模式：1=单向持仓，2=双向持仓',
                               `leverage` int DEFAULT NULL COMMENT '杠杆倍数，支持:1-125',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `close_pnl` decimal(20,8) DEFAULT NULL COMMENT '总盈亏',
                               `open_fee` decimal(20,8) DEFAULT NULL COMMENT '开仓手续费',
                               `close_fee` decimal(20,8) DEFAULT NULL COMMENT '结束手续费',
                               `maker_fee_rate` decimal(20,8) DEFAULT NULL COMMENT '挂单手续费',
                               `taker_fee_rate` decimal(20,8) DEFAULT NULL COMMENT '吃单手续费',
                               `strategy_min_price` decimal(20,8) DEFAULT NULL COMMENT '策略执行最大价格',
                               `strategy_max_price` decimal(20,8) DEFAULT NULL COMMENT '策略执行最小价格',
                               `last_sync_session_id` bigint DEFAULT NULL COMMENT '最后一次同步会话的ID',
                               `trade_type` tinyint DEFAULT '2' COMMENT '交易模式：1=模拟，2=实仓',
                               `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                               PRIMARY KEY (`id`) USING BTREE,
                               KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2001663878197473283 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='应用配置表';

-- ----------------------------
-- Records of app_account
-- ----------------------------
BEGIN;
INSERT INTO `app_account` (`id`, `name`, `user_id`, `exchange`, `api_key`, `api_key_pass`, `init_equity`, `cur_equity`, `equity_coin`, `symbol`, `strategy_instance_id`, `strategy_status`, `remark`, `next_sync_time`, `dual_side_position`, `leverage`, `create_time`, `update_time`, `close_pnl`, `open_fee`, `close_fee`, `maker_fee_rate`, `taker_fee_rate`, `strategy_min_price`, `strategy_max_price`, `last_sync_session_id`, `trade_type`, `deleted`) VALUES (380379106115584, '小洲洲', 381734952633856, 'BINANCE', 'BLt4rsobT99tDqa5pRj5k38A1Wl7IQ0obKfRkA07lktjchCq7k12PG4q6lGzU6jq', 'sAXq79b3q40m5MkLipjUIVMD3YehvBHrVudhiwNtsFthrz58rAq7tSnTdmTAH5gw', 375.04827093, 0.00733631, 'USDT', 'ZECUSDT', 380377721995264, 0, '', '2026-01-12 10:43:28', 2, 20, '2026-01-12 10:44:14', '2026-01-12 10:44:23', 171.08328938, 67.93410906, 48.59667838, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 384258665350656, 2, 0);
INSERT INTO `app_account` (`id`, `name`, `user_id`, `exchange`, `api_key`, `api_key_pass`, `init_equity`, `cur_equity`, `equity_coin`, `symbol`, `strategy_instance_id`, `strategy_status`, `remark`, `next_sync_time`, `dual_side_position`, `leverage`, `create_time`, `update_time`, `close_pnl`, `open_fee`, `close_fee`, `maker_fee_rate`, `taker_fee_rate`, `strategy_min_price`, `strategy_max_price`, `last_sync_session_id`, `trade_type`, `deleted`) VALUES (380387201122304, '沐迁', 381734952633856, 'BINANCE', 'AMcQOxCCzN2xM72uznwUxwpgfMQl2SRmzPBCN39ApymU4TdeviJUZA51ZWTBaZuQ', 'MlbrDHP0fgrX0jhnFz3ylz9dASBYFtRChYGOkWx2SPvJQ6hq02RTMIow3rPLsgxr', 316.45337639, 0.00000000, 'USDT', 'ZECUSDT', 380382683857920, 0, '', '2026-01-12 10:42:55', 2, 20, '2026-01-12 10:44:14', '2026-01-12 10:44:14', 148.85444971, 65.46067558, 44.22629402, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 384663835116544, 2, 0);
INSERT INTO `app_account` (`id`, `name`, `user_id`, `exchange`, `api_key`, `api_key_pass`, `init_equity`, `cur_equity`, `equity_coin`, `symbol`, `strategy_instance_id`, `strategy_status`, `remark`, `next_sync_time`, `dual_side_position`, `leverage`, `create_time`, `update_time`, `close_pnl`, `open_fee`, `close_fee`, `maker_fee_rate`, `taker_fee_rate`, `strategy_min_price`, `strategy_max_price`, `last_sync_session_id`, `trade_type`, `deleted`) VALUES (380758657073152, '77', 381734952633856, 'BINANCE', 'foJmryu4yqtdecO82QE1L7C4ADjW0PuuqcF981FVsJpmGgpmLEH9pcPjivwPDqUC', 'NxMTaedRuz8ZNjM6T8wQhHLaz70nKwawMgEjtvkjTYMGMTYxfC4P3JhtrPudqGBU', 358.74354783, 0.00000000, 'USDT', 'ZECUSDT', 380382683857921, 0, '', '2026-01-12 10:43:03', 2, 20, '2026-01-12 10:44:14', '2026-01-12 10:44:32', 296.95165889, 100.15162537, 86.98350049, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 384684722751488, 2, 0);
INSERT INTO `app_account` (`id`, `name`, `user_id`, `exchange`, `api_key`, `api_key_pass`, `init_equity`, `cur_equity`, `equity_coin`, `symbol`, `strategy_instance_id`, `strategy_status`, `remark`, `next_sync_time`, `dual_side_position`, `leverage`, `create_time`, `update_time`, `close_pnl`, `open_fee`, `close_fee`, `maker_fee_rate`, `taker_fee_rate`, `strategy_min_price`, `strategy_max_price`, `last_sync_session_id`, `trade_type`, `deleted`) VALUES (380758766125056, '19', 381734952633856, 'BINANCE', '7OIbZAoIXInmIR6FcWOli31E8jhXsocLXdajoC4RHYsGuZm5s9y2RuIEcDKEEUPd', 'pqBd61lh2cnA9oKcyURX1X8Y2894aRvgnTkRqzLdHdQl0xAUaKLUKyggF83Z9l9U', 357.48454889, 0.00942494, 'USDT', 'ZECUSDT', 380382683857922, 0, '', '2026-01-12 10:42:37', 2, 20, '2026-01-12 10:44:14', '2026-01-12 10:44:34', 196.29161611, 77.75781785, 53.78090389, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 384258078147584, 2, 0);
INSERT INTO `app_account` (`id`, `name`, `user_id`, `exchange`, `api_key`, `api_key_pass`, `init_equity`, `cur_equity`, `equity_coin`, `symbol`, `strategy_instance_id`, `strategy_status`, `remark`, `next_sync_time`, `dual_side_position`, `leverage`, `create_time`, `update_time`, `close_pnl`, `open_fee`, `close_fee`, `maker_fee_rate`, `taker_fee_rate`, `strategy_min_price`, `strategy_max_price`, `last_sync_session_id`, `trade_type`, `deleted`) VALUES (380758904537088, '不在', 381734952633856, 'BINANCE', 'tdZ5gTKXuvaHjEi6OaVbU6gXBasUHRWHLrEa5FqeMo3n7fvdxb4KPH7HDiL1Akys', 'QzkSHSQsvJOglRWBpPpYG5dhZ2XPoM3gvI2qvfEftWw8nK8R4umBXAnO67gDaiCt', 364.42339133, 0.00725283, 'USDT', 'ZECUSDT', 380382683857923, 0, '', '2026-01-12 10:43:21', 2, 20, '2026-01-12 10:44:14', '2026-01-12 10:44:36', 64.42116962, 28.87625032, 18.10693229, 0.00000000, 0.00000000, 0.00000000, 0.00000000, 384325526749184, 2, 0);
COMMIT;

-- ----------------------------
-- Table structure for app_account_order
-- ----------------------------
DROP TABLE IF EXISTS `app_account_order`;
CREATE TABLE `app_account_order` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `user_id` bigint NOT NULL COMMENT '用户ID',
                                     `account_id` bigint NOT NULL COMMENT '账号ID',
                                     `strategy_instance_id` bigint NOT NULL COMMENT '策略ID',
                                     `position_side` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '持仓方向：LONG=多，SHORT=空',
                                     `buy_side` varchar(16) DEFAULT NULL COMMENT '买卖方向：BUY=买入，SELL=卖出',
                                     `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易平台',
                                     `symbol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易对',
                                     `session_id` bigint NOT NULL COMMENT '会话ID',
                                     `order_id` bigint DEFAULT NULL COMMENT '订单ID',
                                     `client_order_id` varchar(32) NOT NULL COMMENT '业务订单ID',
                                     `order_type` int NOT NULL COMMENT '订单类型：1=开仓，2=平仓',
                                     `expect_price` decimal(20,8) NOT NULL COMMENT '期望价格',
                                     `ave_price` decimal(20,8) DEFAULT NULL COMMENT '实际成交均价',
                                     `qty` decimal(20,8) DEFAULT NULL COMMENT '数量',
                                     `cum_quote` decimal(20,8) DEFAULT NULL COMMENT '成交金额',
                                     `close_pnl` decimal(20,8) DEFAULT NULL COMMENT '盈亏',
                                     `fee` decimal(20,8) DEFAULT NULL COMMENT '手续费',
                                     `status` int NOT NULL COMMENT '状态,和binance状态保持一致',
                                     `sync_status` tinyint NOT NULL DEFAULT '0' COMMENT '最终数据同步：1=完成，0=未完成',
                                     `next_check_time` datetime DEFAULT NULL COMMENT '下次执行时间',
                                     `mock_data` tinyint DEFAULT NULL COMMENT '是否mock数据：1=是，0=否',
                                     `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注说明',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `deleted` tinyint NOT NULL COMMENT '软删除：0=正常，1=已经删除',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     KEY `idx_user_account` (`user_id`,`account_id`) USING BTREE,
                                     KEY `idx_status` (`status`) USING BTREE,
                                     KEY `idx_next_check_time` (`next_check_time`) USING BTREE,
                                     KEY `idx_create_time_accountId` (`create_time`,`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账户交易会话表';

-- ----------------------------
-- Records of app_account_order
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for app_account_session
-- ----------------------------
DROP TABLE IF EXISTS `app_account_session`;
CREATE TABLE `app_account_session` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `user_id` bigint NOT NULL COMMENT '用户ID',
                                       `account_id` bigint NOT NULL COMMENT '账号ID',
                                       `strategy_instance_id` bigint NOT NULL COMMENT '策略ID',
                                       `strategy_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属策略',
                                       `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易平台',
                                       `symbol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易对',
                                       `hold_ave_price` decimal(20,8) DEFAULT NULL COMMENT '持仓均价',
                                       `hold_qty` decimal(20,8) DEFAULT NULL COMMENT '持仓数量',
                                       `close_pnl` decimal(20,8) DEFAULT NULL COMMENT '盈亏',
                                       `open_fee` decimal(20,8) DEFAULT NULL COMMENT '开仓手续费',
                                       `close_fee` decimal(20,8) DEFAULT NULL COMMENT '止盈/止损手续费',
                                       `take_profit_price` decimal(20,8) DEFAULT NULL COMMENT '止盈价格',
                                       `take_profit_client_order_id` varchar(64) DEFAULT NULL COMMENT '止盈业务订单ID',
                                       `take_profit_order_id` varchar(64) DEFAULT NULL COMMENT '止盈订单ID',
                                       `take_profit_algo_id` bigint DEFAULT NULL COMMENT '止盈条件单ID',
                                       `stop_loss_price` decimal(20,8) DEFAULT NULL COMMENT '止损价格',
                                       `stop_loss_client_order_id` varchar(64) DEFAULT NULL COMMENT '止损业务订单ID',
                                       `stop_loss_order_id` varchar(64) DEFAULT NULL COMMENT '止损订单ID',
                                       `stop_loss_algo_id` bigint DEFAULT NULL COMMENT '止损条件单ID',
                                       `base_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '基础参数',
                                       `run_param` varchar(255) DEFAULT NULL COMMENT '运行参数',
                                       `biz_param` varchar(1000) DEFAULT NULL COMMENT '业务参数',
                                       `status` int NOT NULL COMMENT '状态',
                                       `biz_status` int DEFAULT NULL COMMENT '业务状态',
                                       `sync_status` tinyint NOT NULL DEFAULT '0' COMMENT '最终数据同步：1=完成，0=未完成',
                                       `next_check_time` datetime DEFAULT NULL COMMENT '下次执行时间',
                                       `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注说明',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `deleted` tinyint NOT NULL COMMENT '软删除：0=正常，1=已经删除',
                                       `mock_data` tinyint DEFAULT NULL COMMENT '是否mock数据：1=是，0=否',
                                       PRIMARY KEY (`id`) USING BTREE,
                                       KEY `idx_user_account` (`user_id`,`account_id`) USING BTREE,
                                       KEY `idx_status` (`status`) USING BTREE,
                                       KEY `idx_next_check_time` (`next_check_time`) USING BTREE,
                                       KEY `idx_create_time_accountId` (`create_time`,`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账户交易会话表';

-- ----------------------------
-- Records of app_account_session
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for app_config
-- ----------------------------
DROP TABLE IF EXISTS `app_config`;
CREATE TABLE `app_config` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                              `param_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '键',
                              `param_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '值',
                              `status` tinyint DEFAULT '1' COMMENT '状态：1=成功，0=失败',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                              PRIMARY KEY (`id`) USING BTREE,
                              UNIQUE KEY `udx_key` (`param_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='应用配置表';

-- ----------------------------
-- Records of app_config
-- ----------------------------
BEGIN;
INSERT INTO `app_config` (`id`, `param_key`, `param_value`, `status`, `create_time`, `update_time`, `deleted`) VALUES (1, 'ALL_SYMBOL', '[\"ETHUSDT\",\"ZECUSDT\",\"SOLUSDT\"]', 1, '2025-12-23 22:54:09', '2026-01-10 00:53:18', 0);
COMMIT;

-- ----------------------------
-- Table structure for app_strategy
-- ----------------------------
DROP TABLE IF EXISTS `app_strategy`;
CREATE TABLE `app_strategy` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '策略名称',
                                `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '策略编码',
                                `base_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '基础参数',
                                `run_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '运行参数',
                                `symbols` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支持的交易对列表:["BNBUSDT", "ETHUSDC"]',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE KEY `udx_key` (`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='应用配置表';

-- ----------------------------
-- Records of app_strategy
-- ----------------------------
BEGIN;
INSERT INTO `app_strategy` (`id`, `name`, `code`, `base_param`, `run_param`, `symbols`, `create_time`, `update_time`, `deleted`) VALUES (1, '[1m]K线', 'ONE_MIN_SHORT', '{\"mdis\":0.25,\"mldif\":0.1,\"rsdis\":2,\"mitl\": 20, \"rdis5\": 4,\"rdis55m\":6}', '{\"tp\":0.85,\"sl\":100,\"opqty\":1.3,\"appends\":[{\"los\":2.5,\"qty\":3.9}],\"rslos\":5}', '[\"ETHUSDT\",\"ZECUSDT\",\"SOLUSDT\"]', '2025-12-23 22:54:09', '2026-01-12 10:59:14', 0);
COMMIT;

-- ----------------------------
-- Table structure for app_strategy_execution_log
-- ----------------------------
DROP TABLE IF EXISTS `app_strategy_execution_log`;
CREATE TABLE `app_strategy_execution_log` (
                                              `id` bigint NOT NULL COMMENT '日志ID',
                                              `user_id` bigint DEFAULT NULL COMMENT '用户ID',
                                              `account_id` bigint DEFAULT NULL COMMENT '账号ID',
                                              `strategy_instance_id` bigint DEFAULT NULL COMMENT '策略实例ID',
                                              `session_id` bigint DEFAULT NULL COMMENT '会话ID',
                                              `order_id` bigint DEFAULT NULL COMMENT '订单ID',
                                              `strategy_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '策略编码',
                                              `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '交易平台',
                                              `symbol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '交易对',
                                              `log_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '日志类型',
                                              `log_category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '日志类别：SIGNAL/OPEN/APPEND/REVERSE/TAKE_PROFIT/STOP_LOSS/SESSION/STRATEGY/ORDER/SYSTEM',
                                              `log_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'INFO' COMMENT '日志级别：INFO/WARN/ERROR',
                                              `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '日志标题',
                                              `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '日志内容',
                                              `current_price` decimal(20,8) DEFAULT NULL COMMENT '当前价格',
                                              `target_price` decimal(20,8) DEFAULT NULL COMMENT '目标价格',
                                              `hold_qty` decimal(20,8) DEFAULT NULL COMMENT '持仓数量',
                                              `hold_ave_price` decimal(20,8) DEFAULT NULL COMMENT '持仓均价',
                                              `pnl` decimal(20,8) DEFAULT NULL COMMENT '盈亏金额',
                                              `position_side` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '持仓方向：LONG/SHORT',
                                              `result` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '执行结果：SUCCESS/FAILED/SKIPPED',
                                              `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '错误信息',
                                              `ext_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '扩展信息（JSON格式）',
                                              `mock_data` tinyint DEFAULT '0' COMMENT '是否模拟数据：1=是，0=否',
                                              `execution_time` bigint DEFAULT NULL COMMENT '执行耗时（毫秒）',
                                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              `deleted` tinyint DEFAULT '0' COMMENT '是否删除：1=是，0=否',
                                              PRIMARY KEY (`id`) USING BTREE,
                                              KEY `idx_user_id` (`user_id`) USING BTREE,
                                              KEY `idx_account_id` (`account_id`) USING BTREE,
                                              KEY `idx_session_id` (`session_id`) USING BTREE,
                                              KEY `idx_strategy_instance_id` (`strategy_instance_id`) USING BTREE,
                                              KEY `idx_log_type` (`log_type`) USING BTREE,
                                              KEY `idx_log_category` (`log_category`) USING BTREE,
                                              KEY `idx_log_level` (`log_level`) USING BTREE,
                                              KEY `idx_symbol` (`symbol`) USING BTREE,
                                              KEY `idx_create_time` (`create_time`) USING BTREE,
                                              KEY `idx_account_create` (`account_id`,`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='策略执行日志表';

-- ----------------------------
-- Records of app_strategy_execution_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for app_strategy_instance
-- ----------------------------
DROP TABLE IF EXISTS `app_strategy_instance`;
CREATE TABLE `app_strategy_instance` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                         `strategy_id` bigint NOT NULL COMMENT '所属策略模板ID',
                                         `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '策略名称',
                                         `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '策略编码',
                                         `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '所属平台',
                                         `base_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '基础参数',
                                         `run_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '运行参数',
                                         `symbol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支持的交易对：ETHUSDT',
                                         `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注说明',
                                         `status` tinyint DEFAULT NULL COMMENT '上下架状态：1=已上架，2=已下架',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                                         PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=381123225976842 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='应用配置表';

-- ----------------------------
-- Records of app_strategy_instance
-- ----------------------------
BEGIN;
INSERT INTO `app_strategy_instance` (`id`, `strategy_id`, `name`, `code`, `exchange`, `base_param`, `run_param`, `symbol`, `remark`, `status`, `create_time`, `update_time`, `deleted`) VALUES (380377721995243, 1, '[1m]K线', 'ONE_MIN_SHORT', 'BINANCE', '{\"mdis\":0.1,\"mitl\":20,\"mldif\":0.05,\"rdis5\":1,\"rdis55m\":3,\"rsdis\":0.73}', '{\"tp\":0.28,\"sl\":100,\"opqty\":5,\"appends\":[{\"los\":1.1,\"qty\":15}],\"rslos\":2,\"sbuf\":0.25,\"lbuf\":0.25}', 'SOLUSDT', NULL, 1, '2026-01-12 11:01:35', '2026-01-12 11:01:48', 0);
INSERT INTO `app_strategy_instance` (`id`, `strategy_id`, `name`, `code`, `exchange`, `base_param`, `run_param`, `symbol`, `remark`, `status`, `create_time`, `update_time`, `deleted`) VALUES (380377721995264, 1, '[1m]K线-小洲洲', 'ONE_MIN_SHORT', 'BINANCE', '{\"mdis\":0.32,\"mitl\":20,\"mldif\":0.1,\"rdis5\":3,\"rdis55m\":10,\"rsdis\":2}', '{\"tp\":0.85,\"sl\":100,\"opqty\":1.6,\"appends\":[{\"los\":3.5,\"qty\":4.8}],\"rslos\":4.5,\"sbuf\":0.25,\"lbuf\":0.25}', 'ZECUSDT', NULL, 1, '2026-01-12 10:58:25', '2026-01-12 11:01:48', 0);
INSERT INTO `app_strategy_instance` (`id`, `strategy_id`, `name`, `code`, `exchange`, `base_param`, `run_param`, `symbol`, `remark`, `status`, `create_time`, `update_time`, `deleted`) VALUES (380382683857920, 1, '[1m]K线-沐迁', 'ONE_MIN_SHORT', 'BINANCE', '{\"mdis\":0.32,\"mitl\":20,\"mldif\":0.1,\"rdis5\":3,\"rdis55m\":10,\"rsdis\":2}', '{\"tp\":0.88,\"sl\":100,\"opqty\":1.6,\"appends\":[{\"los\":4,\"qty\":4.8}],\"rslos\":5,\"sbuf\":0.25,\"lbuf\":0.25}', 'ZECUSDT', NULL, 1, '2026-01-12 10:58:21', '2026-01-12 11:01:48', 0);
INSERT INTO `app_strategy_instance` (`id`, `strategy_id`, `name`, `code`, `exchange`, `base_param`, `run_param`, `symbol`, `remark`, `status`, `create_time`, `update_time`, `deleted`) VALUES (380382683857921, 1, '[1m]K线-77', 'ONE_MIN_SHORT', 'BINANCE', '{\"mdis\":0.32,\"mitl\":20,\"mldif\":0.1,\"rdis5\":3,\"rdis55m\":10,\"rsdis\":2}', '{\"tp\":0.75,\"sl\":100,\"opqty\":1.7,\"appends\":[{\"los\":4,\"qty\":5}],\"rslos\":5,\"sbuf\":0.25,\"lbuf\":0.25}', 'ZECUSDT', NULL, 1, '2026-01-12 10:58:18', '2026-01-12 11:01:48', 0);
INSERT INTO `app_strategy_instance` (`id`, `strategy_id`, `name`, `code`, `exchange`, `base_param`, `run_param`, `symbol`, `remark`, `status`, `create_time`, `update_time`, `deleted`) VALUES (380382683857922, 1, '[1m]K线-19', 'ONE_MIN_SHORT', 'BINANCE', '{\"mdis\":0.32,\"mitl\":20,\"mldif\":0.1,\"rdis5\":3,\"rdis55m\":10,\"rsdis\":2}', '{\"tp\":0.85,\"sl\":100,\"opqty\":1.8,\"appends\":[{\"los\":4.8,\"qty\":4.5}],\"rslos\":5,\"sbuf\":0.25,\"lbuf\":0.25}', 'ZECUSDT', NULL, 1, '2026-01-12 10:58:14', '2026-01-12 11:01:48', 0);
INSERT INTO `app_strategy_instance` (`id`, `strategy_id`, `name`, `code`, `exchange`, `base_param`, `run_param`, `symbol`, `remark`, `status`, `create_time`, `update_time`, `deleted`) VALUES (380382683857923, 1, '[1m]K线-不在', 'ONE_MIN_SHORT', 'BINANCE', '{\"mdis\":0.32,\"mitl\":20,\"mldif\":0.1,\"rdis5\":3,\"rdis55m\":10,\"rsdis\":2}', '{\"tp\":0.9,\"sl\":100,\"opqty\":1.6,\"appends\":[{\"los\":4.6,\"qty\":4.5}],\"rslos\":4,\"sbuf\":0.25,\"lbuf\":0.25}', 'ZECUSDT', NULL, 1, '2026-01-12 10:58:06', '2026-01-12 11:01:48', 0);
INSERT INTO `app_strategy_instance` (`id`, `strategy_id`, `name`, `code`, `exchange`, `base_param`, `run_param`, `symbol`, `remark`, `status`, `create_time`, `update_time`, `deleted`) VALUES (380382683857942, 1, '[1m]K线', 'ONE_MIN_SHORT', 'BINANCE', '{\"mdis\":1.8,\"mitl\":20,\"mldif\":1,\"rdis5\":20,\"rdis55m\":100,\"rsdis\":18}', '{\"tp\":3,\"sl\":100,\"opqty\":0.5,\"appends\":[{\"los\":15,\"qty\":1.5}],\"rslos\":35,\"sbuf\":0.25,\"lbuf\":0.25}', 'ETHUSDT', NULL, 1, '2026-01-12 10:58:41', '2026-01-12 11:01:48', 0);
COMMIT;

-- ----------------------------
-- Table structure for app_user
-- ----------------------------
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                            `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名称',
                            `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1' COMMENT '用户名',
                            `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1' COMMENT '密码',
                            `salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'IP',
                            `user_type` tinyint NOT NULL COMMENT '用户类型',
                            `status` tinyint NOT NULL COMMENT '状态：1=正常，2=禁用',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=381735187513345 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='应用配置表';

-- ----------------------------
-- Records of app_user
-- ----------------------------
BEGIN;
INSERT INTO `app_user` (`id`, `name`, `username`, `password`, `salt`, `user_type`, `status`, `create_time`, `update_time`, `deleted`) VALUES (381734952633856, 'admin', 'ma123456', 'sX8PrPinc6GhylzdkMU+vc51w79o1iBQi03wFAlVrNw=', 'v66KSiIP+ZLQGEy94SMDEA==', 0, 1, '2026-01-12 11:02:28', '2026-01-12 11:02:38', 0);
COMMIT;

-- ----------------------------
-- Table structure for stat_account_equity_log_day
-- ----------------------------
DROP TABLE IF EXISTS `stat_account_equity_log_day`;
CREATE TABLE `stat_account_equity_log_day` (
                                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                               `user_id` bigint NOT NULL COMMENT '键',
                                               `account_id` bigint NOT NULL COMMENT '键',
                                               `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易所',
                                               `equity` decimal(20,10) NOT NULL DEFAULT '0.0000000000' COMMENT '账户净值',
                                               `time_long` bigint NOT NULL COMMENT '日期：yyyyMMdd',
                                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                                               PRIMARY KEY (`id`) USING BTREE,
                                               UNIQUE KEY `udx_acc_minute_exchange` (`account_id`,`exchange`,`time_long`) USING BTREE,
                                               KEY `idx_user_id` (`user_id`) USING BTREE,
                                               KEY `idx_time_long` (`time_long`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='净值历史：天';

-- ----------------------------
-- Records of stat_account_equity_log_day
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for stat_account_equity_log_hour
-- ----------------------------
DROP TABLE IF EXISTS `stat_account_equity_log_hour`;
CREATE TABLE `stat_account_equity_log_hour` (
                                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                                `user_id` bigint NOT NULL COMMENT '键',
                                                `account_id` bigint NOT NULL COMMENT '键',
                                                `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易所',
                                                `equity` decimal(20,10) NOT NULL DEFAULT '0.0000000000' COMMENT '账户净值',
                                                `time_long` bigint NOT NULL COMMENT '日期：yyyyMMddHH',
                                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                                                PRIMARY KEY (`id`) USING BTREE,
                                                UNIQUE KEY `udx_acc_minute_exchange` (`account_id`,`exchange`,`time_long`) USING BTREE,
                                                KEY `idx_user_id` (`user_id`) USING BTREE,
                                                KEY `idx_time_long` (`time_long`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='净值历史：小时';

-- ----------------------------
-- Records of stat_account_equity_log_hour
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for stat_account_equity_log_minute
-- ----------------------------
DROP TABLE IF EXISTS `stat_account_equity_log_minute`;
CREATE TABLE `stat_account_equity_log_minute` (
                                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                                  `user_id` bigint NOT NULL COMMENT '键',
                                                  `account_id` bigint NOT NULL COMMENT '键',
                                                  `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易所',
                                                  `equity` decimal(20,10) NOT NULL DEFAULT '0.0000000000' COMMENT '账户净值',
                                                  `time_long` bigint NOT NULL COMMENT '日期：yyyyMMddHHmm',
                                                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                                                  PRIMARY KEY (`id`) USING BTREE,
                                                  UNIQUE KEY `udx_acc_minute_exchange` (`account_id`,`exchange`,`time_long`) USING BTREE,
                                                  KEY `idx_user_id` (`user_id`) USING BTREE,
                                                  KEY `idx_time_long` (`time_long`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='净值历史：分钟';

-- ----------------------------
-- Records of stat_account_equity_log_minute
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for stat_user_equity_log_day
-- ----------------------------
DROP TABLE IF EXISTS `stat_user_equity_log_day`;
CREATE TABLE `stat_user_equity_log_day` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                            `user_id` bigint NOT NULL COMMENT '键',
                                            `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易所',
                                            `equity` decimal(20,10) NOT NULL DEFAULT '0.0000000000' COMMENT '账户净值',
                                            `time_long` bigint NOT NULL COMMENT '日期：yyyyMMdd',
                                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                                            PRIMARY KEY (`id`) USING BTREE,
                                            UNIQUE KEY `udx_acc_minute_exchange` (`user_id`,`exchange`,`time_long`) USING BTREE,
                                            KEY `idx_user_id` (`user_id`) USING BTREE,
                                            KEY `idx_time_long` (`time_long`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='净值历史：天';

-- ----------------------------
-- Records of stat_user_equity_log_day
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for stat_user_equity_log_hour
-- ----------------------------
DROP TABLE IF EXISTS `stat_user_equity_log_hour`;
CREATE TABLE `stat_user_equity_log_hour` (
                                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                             `user_id` bigint NOT NULL COMMENT '键',
                                             `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易所',
                                             `equity` decimal(20,10) NOT NULL DEFAULT '0.0000000000' COMMENT '账户净值',
                                             `time_long` bigint NOT NULL COMMENT '日期：yyyyMMdd',
                                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                                             PRIMARY KEY (`id`) USING BTREE,
                                             UNIQUE KEY `udx_acc_minute_exchange` (`user_id`,`exchange`,`time_long`) USING BTREE,
                                             KEY `idx_user_id` (`user_id`) USING BTREE,
                                             KEY `idx_time_long` (`time_long`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='净值历史：时';

-- ----------------------------
-- Records of stat_user_equity_log_hour
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for stat_user_equity_log_minute
-- ----------------------------
DROP TABLE IF EXISTS `stat_user_equity_log_minute`;
CREATE TABLE `stat_user_equity_log_minute` (
                                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                               `user_id` bigint NOT NULL COMMENT '键',
                                               `exchange` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易所',
                                               `equity` decimal(20,10) NOT NULL DEFAULT '0.0000000000' COMMENT '账户净值',
                                               `time_long` bigint NOT NULL COMMENT '日期：yyyyMMdd',
                                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标志，0: 未删除，1: 已删除',
                                               PRIMARY KEY (`id`) USING BTREE,
                                               UNIQUE KEY `udx_acc_minute_exchange` (`user_id`,`exchange`,`time_long`) USING BTREE,
                                               KEY `idx_user_id` (`user_id`) USING BTREE,
                                               KEY `idx_time_long` (`time_long`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='净值历史：分';

-- ----------------------------
-- Records of stat_user_equity_log_minute
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
