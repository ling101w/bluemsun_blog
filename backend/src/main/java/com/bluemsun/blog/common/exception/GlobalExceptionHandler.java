package com.bluemsun.blog.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.bluemsun.blog.common.core.ApiCode;
import com.bluemsun.blog.common.core.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理，保证接口响应格式一致。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ApiResponse.fail(ex.getApiCode(), ex.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ApiResponse<Void> handleValidationException(Exception ex) {
        log.warn("参数校验失败", ex);
        return ApiResponse.fail(ApiCode.BAD_REQUEST, "参数校验失败，请检查输入" + formatMessage(ex));
    }

    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLoginException(NotLoginException ex) {
        return ApiResponse.fail(ApiCode.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<Void> handleNoPermission(NotPermissionException ex) {
        return ApiResponse.fail(ApiCode.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleAnyException(Exception ex) {
        log.error("系统异常", ex);
        return ApiResponse.fail(ApiCode.SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("文件上传超过大小限制", ex);
        return ApiResponse.fail(ApiCode.BAD_REQUEST, "上传文件过大，请压缩或分批上传");
    }

    private String formatMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null ? "" : (":" + message);
    }
}

