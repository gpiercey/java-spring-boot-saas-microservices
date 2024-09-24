package com.piercey.controllers;

import com.piercey.ExecutionTimer;
import com.piercey.exceptions.Http400Exception;
import com.piercey.exceptions.Http404Exception;
import com.piercey.models.UserData;
import com.piercey.models.UserStatus;
import com.piercey.repositories.UserDataRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
class UserDataController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserDataRepository repository;

    @RequestMapping(value = "/api/user", method = POST)
    @CachePut(value = "userdata", key = "#o.id")
    public UserData createOne(
            final @NonNull @RequestBody UserData o,
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            sanitizeUserInput(o);
            enforcePermissions(request, POST);
            o.setStatus(UserStatus.ACTIVE);
            return upsert(o);
        }
    }

    // TODO: you'll want to implement a paged response for this before releasing it into the wild
    @RequestMapping(value = "/api/users", method = GET)
    public List<UserData> readAll(
            final @RequestParam(value = "username", required = false) String username,
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            enforcePermissions(request, GET);

            if (Strings.isNotBlank(username)) {
                final UserData result = repository.findByUsername(username);
                return result != null
                        ? List.of(result)
                        : List.of();
            }

            return repository.findAll();
        }
    }

    @RequestMapping(value = "/api/user/{id}", method = GET)
    @Cacheable(value = "userdata", key = "#id")
    public UserData readOne(
            final @PathVariable String id,
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            enforcePermissions(request, GET);
            return repository
                    .findById(sanitizeUserInput(id))
                    .orElseThrow(() -> new Http404Exception("user data not found for " + id));
        }
    }

    @RequestMapping(value = "/api/user", method = GET)
    public UserData queryOneBy(
            final @RequestParam(required = false) String username,
            final @RequestParam(required = false) String email,
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            enforcePermissions(request, GET);

            if (Strings.isNotBlank(username)) {
                return repository.findByUsername(username);
            }

            if (Strings.isNotBlank(email)) {
                return repository.findByEmail(email);
            }

            throw new Http400Exception("query parameters are required");
        }
    }

    @RequestMapping(value = "/api/user/{id}", method = PUT)
    @CachePut(value = "userdata", key = "#o.id")
    public UserData updateOne(
            final @NonNull @RequestBody UserData o,
            final @NonNull @PathVariable String id,
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            sanitizeUserInput(o);
            enforcePermissions(request, PUT);
            return upsert(o.setId(sanitizeUserInput(id)));
        }
    }

    @RequestMapping(value = "/api/user/{id}", method = DELETE)
    @CacheEvict(value = "userdata", key = "#id")
    public void deleteOne(
            final @NonNull @PathVariable String id,
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            enforcePermissions(request, DELETE);
            repository.findById(sanitizeUserInput(id)).map(o -> {
                o.setStatus(UserStatus.ARCHIVED);
                repository.save(o);
                return null;
            });
        }
    }

    private UserData upsert(final @NonNull UserData o) {
        return repository.findById(o.getId()).map(userData -> {
            if (Strings.isNotBlank(o.getUsername())) {
                userData.setUsername(o.getUsername());
            }
            if (Strings.isNotBlank(o.getFullname())) {
                userData.setFullname(o.getFullname());
            }
            if (Strings.isNotBlank(o.getEmail())) {
                userData.setEmail(o.getEmail());
            }
            if (o.getStatus() != null) {
                userData.setStatus(o.getStatus());
            }
            if (Strings.isNotBlank(o.getCompanyId())) {
                userData.setCompanyId(o.getCompanyId());
            }
            return repository.save(userData);
        }).orElseGet(() -> repository.save(o));
    }

    private String extractConnInfo(final @NonNull HttpServletRequest request) {
        return String.format("%s:%s%s",
                request.getMethod(),
                request.getRequestURI(),
                Strings.isNotBlank(request.getQueryString()) ? "?" + request.getQueryString() : "");
    }

    private String extractTraceInfo(final @NonNull HttpServletRequest request) {
        return String.format("(%s %s %s)%s",
                request.getProtocol(),
                request.getRemoteAddr(),
                request.getHeader("user-agent"),
                Strings.isNotBlank(request.getHeader("x-trace-id")) ? " trace:" + request.getHeader("x-trace-id") : "");
    }

    private void sanitizeUserInput(final @NonNull UserData o) {
        Arrays.stream(UserData.class.getDeclaredFields())
                .filter(field -> field.getType().equals(String.class))
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        if (Strings.isNotBlank((String) field.get(o))) {
                            field.set(o, Jsoup.clean((String) field.get(o), Safelist.basic()));
                        }
                    } catch (IllegalAccessException e) {
                        throw new Http400Exception(e);
                    }
                });
    }

    private String sanitizeUserInput(final @NonNull String s) {
        return Jsoup.clean(s, Safelist.basic());
    }

    private void enforcePermissions(final @NonNull HttpServletRequest request, final @NonNull RequestMethod method) {
        // TODO: extract token and check permissions
    }
}