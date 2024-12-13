package com.loanapi.model;

import com.loanapi.Translator;

public class LoanApiException extends Exception{

    protected LoanApiErrorCode code;
    protected String description;

    public LoanApiException() {
    }

    public LoanApiException(LoanApiErrorCode errorCode) {
        this.code = errorCode;
        this.description = Translator.toLocale("error.code." + errorCode);
    }

    public LoanApiException(String errorCode) {
        this.code = LoanApiErrorCode.valueOf(errorCode);
        this.description = Translator.toLocale("error.code." + errorCode);
    }

    public LoanApiErrorCode getCode() {
        return code;
    }

    public void setCode(LoanApiErrorCode code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static String getLocalizedMessage(LoanApiErrorCode errorCode) {
        return Translator.toLocale("error.code." + errorCode);
    }

    @Override
    public String getMessage() {
        return getDescription();
    }


}
