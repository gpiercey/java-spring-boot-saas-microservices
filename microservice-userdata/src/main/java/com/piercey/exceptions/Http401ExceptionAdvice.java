package com.piercey.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class Http401ExceptionAdvice {
    @ExceptionHandler(Http401Exception.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handler(Http401Exception e) {
        return e.hasCustomData()
                ? String.format("{ \"Message\": \"%s\", \"Meta\": \"%d %s\" }", e.getHttpMessage(), e.getCustomStatus(), e.getCustomMessage())
                : String.format("{ \"Message\": \"%s\" }", e.getMessage());
    }
}