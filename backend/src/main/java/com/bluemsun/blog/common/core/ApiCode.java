package com.bluemsun.blog.common.core;

/**
 * 统一的业务状态码枚举。
 * <p>
 * 后续模块可直接引用，保证前后端状态码口径一致。
 */
public enum ApiCode {

    /** 成功 */
    SUCCESS(0, "操作成功"),

    /** 参数异常 */
    BAD_REQUEST(400, "请求参数错误"),

    /** 未认证或登录失效 */
    UNAUTHORIZED(401, "登录状态失效"),

    /** 无权限 */
    FORBIDDEN(403, "暂无权限"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 系统异常 */
    SERVER_ERROR(500, "系统繁忙，请稍后重试"),

    /** 自定义业务异常 */
    BUSINESS_ERROR(600, "业务异常");

    private final int code;
    private final String message;

    ApiCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

