package com.piercey.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.piercey.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

@RestController
public class BaseController {

    @Autowired
    protected ObjectMapper objectMapper;

    protected void require(final boolean condition, final int code, final @NonNull String message) {
        if (!condition) {
            switch (code) {
                case 400 -> throw new Http400Exception(message);
                case 401 -> throw new Http401Exception(message);
                case 403 -> throw new Http403Exception(message);
                case 404 -> throw new Http404Exception(message);
                default -> throw new Http500Exception(message);
            }
        }
    }

    protected String extractConnInfo(final @NonNull HttpServletRequest request) {
        return String.format("%s:%s%s",
                request.getMethod(),
                request.getRequestURI(),
                Strings.isNotBlank(request.getQueryString()) ? "?" + request.getQueryString() : "");
    }

    protected String extractTraceInfo(final @NonNull HttpServletRequest request) {
        return String.format("(%s %s %s)%s",
                request.getProtocol(),
                request.getRemoteAddr(),
                request.getHeader("user-agent"),
                Strings.isNotBlank(request.getHeader("x-trace-id")) ? " trace:" + request.getHeader("x-trace-id") : "");
    }

    protected String extractTokenFromHeaders(final @NonNull Map<String, String> headers) {
        return headers.getOrDefault(HttpHeaders.AUTHORIZATION.toLowerCase(), "")
                .replaceFirst("Bearer:", "")
                .strip();
    }

    protected String extractUidFromToken(@NonNull String token) {
        try {
            final String content = new String(Base64.getUrlDecoder()
                    .decode(token.split("\\.")[1]));

            return objectMapper.readTree(content)
                    .at("/sub")
                    .asText("");

        } catch (Exception e) {
            return "";
        }
    }

    protected String generateHash(String s) {
        return !com.google.common.base.Strings.isNullOrEmpty(s)
                ? Hashing.sha256().hashString(s, StandardCharsets.UTF_8).toString()
                : "";
    }

    protected void sanitizeUserInput(final @NonNull Object o, final Class<?> cls) {
        Arrays.stream(cls.getDeclaredFields())
                .filter(field -> field.getType().equals(String.class))
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        if (Strings.isNotBlank((String) field.get(o))) {
                            field.set(o, sanitizeUserInput((String) field.get(o)));
                        }
                    } catch (IllegalAccessException e) {
                        throw new Http400Exception(e);
                    }
                });
    }

    protected String sanitizeUserInput(final @NonNull String s) {
        return Jsoup.clean(s, Safelist.basic());
    }
}