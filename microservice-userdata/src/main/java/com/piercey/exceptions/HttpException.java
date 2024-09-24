package com.piercey.exceptions;

public class HttpException extends RuntimeException {
    private final int httpStatus;
    private int customStatus = Integer.MIN_VALUE;
    private String customMessage;

    protected HttpException(final int httpStatus, final String httpMessage) {
        super(httpMessage);
        this.httpStatus = httpStatus;
    }

    protected HttpException(final int httpStatus, final String httpMessage, final Throwable e) {
        super(httpMessage, e);
        this.httpStatus = httpStatus;
    }

    protected HttpException(final int httpStatus, final String httpMessage, final int customStatus, final String customMessage) {
        super(httpMessage);
        this.httpStatus = httpStatus;
        this.customStatus = customStatus;
        this.customMessage = customMessage;
    }

    protected HttpException(final int httpStatus, final String httpMessage, final int customStatus, final String customMessage, final Throwable e) {
        super(httpMessage, e);
        this.httpStatus = httpStatus;
        this.customStatus = customStatus;
        this.customMessage = customMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getHttpMessage() {
        return super.getMessage();
    }

    public int getCustomStatus() {
        return customStatus;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public boolean hasCustomData() {
        return customStatus > Integer.MIN_VALUE || customMessage != null;
    }
}