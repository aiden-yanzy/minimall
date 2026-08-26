package com.minimall.common.result;

import lombok.Getter;

/**
 * 业务状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // ---- 参数 / 校验 ----
    PARAM_ERROR(400, "参数错误"),
    VALIDATE_ERROR(400, "参数校验失败"),

    // ---- 认证授权 ----
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限"),
    TOKEN_INVALID(401, "无效的令牌"),

    // ---- 业务 ----
    NOT_FOUND(404, "资源不存在"),

    // ---- 用户 ----
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXIST(1002, "用户已存在"),
    MOBILE_ALREADY_BOUND(1003, "手机号已被占用"),
    PASSWORD_ERROR(1004, "账号或密码错误"),
    ACCOUNT_DISABLED(1005, "账号已被禁用"),
    OLD_PASSWORD_ERROR(1006, "原密码错误"),

    // ---- 商品 ----
    GOODS_NOT_EXIST(2001, "商品不存在"),
    GOODS_OFF_SHELF(2002, "商品已下架"),
    STOCK_NOT_ENOUGH(2003, "库存不足"),

    // ---- 订单 ----
    ORDER_NOT_EXIST(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态不允许该操作"),

    // ---- 通用兜底 ----
    UNKNOWN_ERROR(9999, "系统繁忙，请稍后再试");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
