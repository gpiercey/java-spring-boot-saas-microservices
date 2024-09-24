package com.piercey.exceptions;

public class Http403Exception extends HttpException {
    private final static int code = 403;
    private final static String message = "Forbidden";

    public Http403Exception() {
        super(code, message);
    }

    public Http403Exception(final String message) {
        super(code, message);
    }

    public Http403Exception(final String message, final Throwable e) {
        super(code, message, e);
    }

    public Http403Exception(final Throwable e) {
        super(code, message, e);
    }

    public Http403Exception(final int customCode, final String customMessage) {
        super(code, message, customCode, customMessage);
    }

    public Http403Exception(final int customCode, final String customMessage, final Throwable e) {
        super(code, message, customCode, customMessage, e);
    }
}