package com.piercey.controllers;

import com.piercey.ExecutionTimer;
import com.piercey.exceptions.Http400Exception;
import com.piercey.exceptions.Http404Exception;
import com.piercey.models.Company;
import com.piercey.models.CompanyStatus;
import com.piercey.repositories.CompanyRepository;
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
class CompanyController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CompanyRepository repository;

    @RequestMapping(value = "/api/company", method = POST)
    @CachePut(value = "company", key = "#o.id")
    public Company createOne(
            final @NonNull @RequestBody Company o,
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            sanitizeUserInput(o);
            enforcePermissions(request, POST);
            o.setStatus(CompanyStatus.ACTIVE);
            return upsert(o);
        }
    }

    // TODO: you'll want to implement a paged response for this before releasing it into the wild
    @RequestMapping(value = "/api/companies", method = GET)
    public List<Company> readAll(
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            enforcePermissions(request, GET);
            return repository.findAll();
        }
    }

    @RequestMapping(value = "/api/company/{id}", method = GET)
    @Cacheable(value = "company", key = "#id")
    public Company readOne(
            final @NonNull @PathVariable String id,
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            enforcePermissions(request, GET);
            return repository
                    .findById(sanitizeUserInput(id))
                    .orElseThrow(() -> new Http404Exception("user data not found for " + id));
        }
    }

    @RequestMapping(value = "/api/company/{id}", method = PUT)
    @CachePut(value = "company", key = "#o.id")
    public Company updateOne(
            final @NonNull @RequestBody Company o,
            final @NonNull @PathVariable String id,
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            sanitizeUserInput(o);
            enforcePermissions(request, PUT);
            return upsert(o.setId(sanitizeUserInput(id)));
        }
    }

    @RequestMapping(value = "/api/company/{id}", method = DELETE)
    @CacheEvict(value = "company", key = "#id")
    public void deleteOne(
            final @NonNull @PathVariable String id,
            final @NonNull HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            enforcePermissions(request, DELETE);
            repository.findById(sanitizeUserInput(id)).map(o -> {
                o.setStatus(CompanyStatus.ARCHIVED);
                repository.save(o);
                return null;
            });
        }
    }

    private Company upsert(final @NonNull Company o) {
        return repository.findById(o.getId()).map(company -> {
            if (Strings.isNotBlank(o.getCompanyName())) {
                company.setCompanyName(o.getCompanyName());
            }
            if (o.getStatus() != null) {
                company.setStatus(o.getStatus());
            }
            return repository.save(company);
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

    private void sanitizeUserInput(final @NonNull Company o) {
        Arrays.stream(Company.class.getDeclaredFields())
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