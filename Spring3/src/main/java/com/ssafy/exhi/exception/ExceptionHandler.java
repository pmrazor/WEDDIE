package com.ssafy.exhi.exception;


import com.ssafy.exhi.base.code.BaseErrorCode;

public class ExceptionHandler extends GeneralException {

    public ExceptionHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}