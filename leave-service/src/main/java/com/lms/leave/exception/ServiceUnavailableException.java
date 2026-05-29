package com.lms.leave.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String msg) {
        super(msg);
    }
}
