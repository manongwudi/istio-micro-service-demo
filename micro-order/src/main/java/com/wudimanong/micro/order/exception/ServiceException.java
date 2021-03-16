package com.wudimanong.micro.order.exception;

/**
 * @author jiangqiao
 */
public class ServiceException extends RuntimeException {

    private final Integer code;

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(Integer code, String message, Throwable e) {
        super(message, e);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
