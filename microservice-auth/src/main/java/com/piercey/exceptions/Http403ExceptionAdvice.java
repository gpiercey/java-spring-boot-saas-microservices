package com.piercey.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class Http403ExceptionAdvice {
    @ExceptionHandler(Http403Exception.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handler(Http403Exception e) {
        return e.hasCustomData()
                ? String.format("{ \"Message\": \"%s\", \"Meta\": \"%d %s\" }", e.getHttpMessage(), e.getCustomStatus(), e.getCustomMessage())
                : String.format("{ \"Message\": \"%s\" }", e.getMessage());
    }
}