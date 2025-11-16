package com.bluemsun.blog.common.core;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * REST 接口统一响应结构。
 *
 * @param <T> 数据泛型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 是否成功 */
    private boolean success;
    /** 状态码 */
    private int code;
    /** 文案信息（给前端使用） */
    private String message;
    /** 具体数据内容 */
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, ApiCode apiCode, T data) {
        this.success = success;
        this.code = apiCode.getCode();
        this.message = apiCode.getMessage();
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, ApiCode.SUCCESS, data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>(true, ApiCode.SUCCESS, data);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> fail(ApiCode apiCode) {
        return new ApiResponse<>(false, apiCode, null);
    }

    public static <T> ApiResponse<T> fail(ApiCode apiCode, String message) {
        ApiResponse<T> response = fail(apiCode);
        response.setMessage(message);
        return response;
    }

    // region getter & setter

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // endregion
}

