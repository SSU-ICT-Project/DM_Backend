package com.dm.dmbackend.global.exception;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
    private final ReturnCode returnCode;
    private final String overrideMessage;

    public ServiceException(ReturnCode rc) {
        super(rc.getMessage());
        this.returnCode = rc;
        this.overrideMessage = null;
    }

    public ServiceException(ReturnCode rc, String overrideMessage) {
        super(overrideMessage);
        this.returnCode = rc;
        this.overrideMessage = overrideMessage;
    }
}
