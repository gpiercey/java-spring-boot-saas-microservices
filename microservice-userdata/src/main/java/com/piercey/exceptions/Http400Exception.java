package com.piercey.exceptions;

public class Http400Exception extends HttpException {
    private final static int code = 400;
    private final static String message = "Bad Request";

    public Http400Exception() {
        super(code, message);
    }

    public Http400Exception(final String m) {
        super(code, message);
    }

    public Http400Exception(final String message, final Throwable e) {
        super(code, message, e);
    }

    public Http400Exception(final Throwable e) {
        super(code, message, e);
    }

    public Http400Exception(final int customCode, final String customMessage) {
        super(code, message, customCode, customMessage);
    }

    public Http400Exception(final int customCode, final String customMessage, final Throwable e) {
        super(code, message, customCode, customMessage, e);
    }
}