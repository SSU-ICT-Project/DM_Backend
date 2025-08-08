package com.dm.DM_Backend.global.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
    private final ReturnCode returnCode;

    public ServiceException(ReturnCode returnCode) {
        super(returnCode.getCode() + " : " + returnCode.getMessage());
        this.returnCode = returnCode;
    }
}
