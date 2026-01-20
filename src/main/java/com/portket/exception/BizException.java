package com.portket.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String additionalMessage;

    public BizException(String message, ErrorCode errorCode, String additionalMessage) {
        super(message);
        this.errorCode = errorCode;
        this.additionalMessage = additionalMessage;
    }

    public BizException(ErrorCode errorCode, String additionalMessage) {
        super(errorCode.getCustomMessage());
        this.errorCode = errorCode;
        this.additionalMessage = additionalMessage;
    }
}
