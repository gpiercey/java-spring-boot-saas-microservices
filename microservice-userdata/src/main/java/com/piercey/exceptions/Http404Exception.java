package com.piercey.exceptions;

public class Http404Exception extends HttpException {
    private final static int code = 404;
    private final static String message = "Not Found";

    public Http404Exception() {
        super(code, message);
    }

    public Http404Exception(final String message) {
        super(code, message);
    }

    public Http404Exception(final String message, final Throwable e) {
        super(code, message, e);
    }

    public Http404Exception(final Throwable e) {
        super(code, message, e);
    }

    public Http404Exception(final int customCode, final String customMessage) {
        super(code, message, customCode, customMessage);
    }

    public Http404Exception(final int customCode, final String customMessage, final Throwable e) {
        super(code, message, customCode, customMessage, e);
    }
}