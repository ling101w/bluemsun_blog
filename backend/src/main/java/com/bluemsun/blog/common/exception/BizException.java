package com.bluemsun.blog.common.exception;

import com.bluemsun.blog.common.core.ApiCode;

/**
 * 自定义业务异常，统一携带状态码与提示语。
 */
public class BizException extends RuntimeException {

    private final ApiCode apiCode;

    public BizException(ApiCode apiCode) {
        super(apiCode.getMessage());
        this.apiCode = apiCode;
    }

    public BizException(ApiCode apiCode, String message) {
        super(message);
        this.apiCode = apiCode;
    }

    public ApiCode getApiCode() {
        return apiCode;
    }
}

