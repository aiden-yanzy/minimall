package com.minimall.common.exception;

import com.minimall.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常：可在 Service 层直接抛出，由全局异常处理器统一封装
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String msg) {
        super(msg);
        this.code = ResultCode.FAIL.getCode();
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }
}
