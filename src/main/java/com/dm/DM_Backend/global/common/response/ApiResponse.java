package com.dm.DM_Backend.global.common.response;

import com.dm.DM_Backend.global.exception.ReturnCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private ReturnCode returnCode;
    private String returnMessage;
    private T data;
    private DMPage<T> dmPage;

    public static <T> ApiResponse of(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.returnCode = ReturnCode.SUCCESS;
        response.returnMessage = ReturnCode.SUCCESS.getMessage();
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> of(DMPage<T> dmPage) {
        ApiResponse<T> response = new ApiResponse<>();
        response.returnCode = ReturnCode.SUCCESS;
        response.returnMessage = ReturnCode.SUCCESS.getMessage();
        response.dmPage = dmPage;
        return response;
    }

    public static <T> ApiResponse<T> of(ReturnCode returnCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.returnCode = returnCode;
        response.returnMessage = returnCode.getMessage();
        return response;
    }
}
