// All rights reserved
package com.wuin.wi_mega.common.exception;


import com.wuin.wi_mega.common.enums.IEnumItem;

/**
 * @version 1.0
 * @created 2023/3/2 下午3:13
 **/
public interface IResponseStatusMsg extends IEnumItem<Integer, String> {
    //通用的枚举类型
    enum APIEnum implements IResponseStatusMsg {
        // 通用码
        SUCCESS(0, "成功"),

        /*
         * 1~999 系统保留, 通用状态码
         */

        /*
         * 1000~1999 通用的服务器异常,thrift RPC之间不进行这类code传递，直接抛异常, 影响API成功率统计
         */
        FAILED(1001, "失败"),
        SERVER_ERROR(1002, "服务器内部错误!"),
        JSON_PARSE_FAILED(1007, "json转换异常!"),
        PARAM_ERROR(2001, "请求参数异常!"),
        KEY_EXIST(2002, "账号对应的API KEY已存在!"),
        ACCOUNT_ID_NOT_EXIST(2003, "账号ID不存在"),

        STRATEGY_IS_RUNNING(2004, "该账号策略正在运行中，请停止后再操作"),

        HAS_NO_STRATEGY(2005, "该账号未配置任何策略"),

        ACCOUNT_NOT_EXIST(3003, "账号不存在或没有权限操作"),
        USER_EXPIRE(3004, "登录已失效，请重新登录"),
        USER_AUTH_FAIL_ROOT(3005, "小伙子你想干嘛"),
        USER_AUTH_FAIL(3006, "越权访问!!"),

        USERNAME_EXIST(3007, "用户名已经被使用!!"),
        USER_NOT_EXIST(3008, "用户不存在!"),
        PASSWORD_ERROR(3009, "密码不正确!!"),
        PASSWORD_NOT_EQUAL(3010, "确认密码和新密码不一致!!"),
        USERNAME_NOT_EXIST(3011, "用户名不存在!!"),

        STRATEGY_NOT_EXIST(3012, "策略模板不存在!!"),
        STRATEGY_NOT_SUPPORT_SYMBOL(3013, "策略模板不支持该交易对!!"),

        INVALID_STRATEGY_INSTANCE_ID(3014, "无效的策略ID!!"),

        HAS_RUNNING_ACCOUNTS(3015, "当前策略有账号正在运行，无法删除!!"),

        STRATEGY_STATUS_CHANGED(3016, "策略状态发生变化，请稍后重试!!"),

        OPEN_FAIL(3017, "加仓异常，请刷新页面查看仓位信息!!"),

        ALREADY_HAS_POSITION(3018, "当前账户已经持仓，请手动处理!!"),

        USER_FORBIDDEN(3019, "该账号已被禁用，请联系管理员!!"),

        USER_HAS_BINDIND_ACCOUNT(3020, "该用户名下存在绑定账号，请先删除账号!!"),

        CANNOT_DELETE_ADMIN(3021, "不能删除管理员账号!!"),

        API_KEY_INVALID(3022, "和平台交互异常，KEY或SECRET无效，请检查后重试!!"),

        HAS_NO_POSITION(3023, "当前没有对应持仓信息"),

        TAKE_PROFIT_PRICE_MUST_GATHER_CUR(3024, "止盈价格必须大于当前价格"),
        TAKE_PROFIT_PRICE_MUST_LESS_CUR(3025, "止盈价格必须小于当前价格"),

        STOP_LOSS_PRICE_MUST_GATHER_CUR(3026, "止损价格必须大于当前价格"),
        STOP_LOSS_PRICE_MUST_LESS_CUR(3027, "止损价格必须小于当前价格"),


        FUCK_MSG(9999, "FUCK!!!"),
        ;

        /**
         * 异常编码
         */
        private Integer code;

        /**
         * 异常提示
         */
        private String message;

        APIEnum(Integer code, String message) {
            this.code = code;
            this.message = message;
        }

        public Integer getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public static boolean isSuccess(Integer code) {
            return SUCCESS.equalByCode(code);
        }
    }

    Integer getCode();

    String getMessage();
}