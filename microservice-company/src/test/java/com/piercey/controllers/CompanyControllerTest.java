package com.piercey.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.piercey.models.Company;
import com.piercey.models.CompanyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.OK;

// TODO: we should really mock redis+mongo for this to be complete but for now I'm just running those in containers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CompanyControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @BeforeEach
    public void setup() {
        // We need to swap the default object mapper so we can configure how enums are converted
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        rest.getRestTemplate()
                .getMessageConverters()
                .removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));

        rest.getRestTemplate()
                .getMessageConverters()
                .add(converter);
    }

    @Test
    void createOne() {
        Company o = new Company()
                .setId(UUID.randomUUID().toString())
                .setStatus(CompanyStatus.SUSPENDED)
                .setCompanyName("zzz");

        Company response = postObject(o);
        assertEquals(o.getId(), response.getId());
        assertEquals(o.getCompanyName(), response.getCompanyName());
        assertEquals(CompanyStatus.ACTIVE.toString(), response.getStatus());
    }

    @Test
    void createOneWithCSSVulnerability() {
        Company o = new Company()
                .setId(UUID.randomUUID().toString())
                .setStatus(CompanyStatus.SUSPENDED)
                .setCompanyName("zzz<script></script>");

        Company response = postObject(o);
        assertEquals(o.getId(), response.getId());
        assertEquals("zzz", response.getCompanyName()); // check that this has been scrubbed
        assertEquals(CompanyStatus.ACTIVE.toString(), response.getStatus());
    }

    @Test
    void readAll() {
        String url = String.format("http://localhost:%d/api/companies", port);

        HttpEntity<Void> request = new HttpEntity<>(null);
        ResponseEntity<Company[]> response = rest.exchange(url, GET, request, Company[].class);
        assertEquals(OK, response.getStatusCode());

        List<Company> companies = List.of(Objects.requireNonNull(response.getBody()));
        assertTrue(companies.stream().anyMatch(o -> "ABC".equals(o.getCompanyName())));
        assertTrue(companies.stream().anyMatch(o -> "DEF".equals(o.getCompanyName())));
    }

    @Test
    void readOne() {
        String url = String.format("http://localhost:%d/api/company/01920ce1-4a4b-7677-a9b4-51f74af864f9", port);
        HttpEntity<Void> request = new HttpEntity<>(null);

        ResponseEntity<Company> response = rest.exchange(url, GET, request, Company.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals("ABC", response.getBody().getCompanyName());
    }

    @Test
    void updateOne() {
        String url = String.format("http://localhost:%d/api/company/01920ce1-4a4b-7325-9cc9-2d6a0bc54185", port);

        HttpEntity<Void> request = new HttpEntity<>(null);
        ResponseEntity<Company> response = rest.exchange(url, GET, request, Company.class);
        assertEquals(OK, response.getStatusCode());

        HttpEntity<Company> updateRequest = new HttpEntity<>(response.getBody().setCompanyName("Blah Blah Blah"));
        ResponseEntity<Company> updateResponse = rest.exchange(url, PUT, updateRequest, Company.class);
        assertEquals(OK, updateResponse.getStatusCode());
        assertEquals("Blah Blah Blah", updateResponse.getBody().getCompanyName());

        response = rest.exchange(url, GET, request, Company.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals("Blah Blah Blah", response.getBody().getCompanyName());
    }

    @Test
    void updateOneWithCSSVulnerability() {
        String url = String.format("http://localhost:%d/api/company/01920ce1-4a4b-7325-9cc9-2d6a0bc54185", port);

        HttpEntity<Void> request = new HttpEntity<>(null);
        ResponseEntity<Company> response = rest.exchange(url, GET, request, Company.class);
        assertEquals(OK, response.getStatusCode());

        HttpEntity<Company> updateRequest = new HttpEntity<>(response.getBody().setCompanyName("Blah Blah Blah<script></script>"));
        ResponseEntity<Company> updateResponse = rest.exchange(url, PUT, updateRequest, Company.class);
        assertEquals(OK, updateResponse.getStatusCode());
        assertEquals("Blah Blah Blah", updateResponse.getBody().getCompanyName());

        response = rest.exchange(url, GET, request, Company.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals("Blah Blah Blah", response.getBody().getCompanyName());
    }

    @Test
    void deleteCompany() {
        String url = String.format("http://localhost:%d/api/company/01920ce1-4a4b-7804-be75-dfad5cb1cbef", port);

        HttpEntity<Void> request = new HttpEntity<>(null);
        ResponseEntity<Void> response = rest.exchange(url, DELETE, request, Void.class);
        assertEquals(OK, response.getStatusCode());

        ResponseEntity<Company> updateResponse = rest.exchange(url, GET, request, Company.class);
        assertEquals(OK, updateResponse.getStatusCode());
        assertEquals(CompanyStatus.ARCHIVED.toString(), updateResponse.getBody().getStatus());
    }

    private Company postObject(final @NonNull Company o) {
        final String url = String.format("http://localhost:%d/api/company", port);

        final HttpEntity<Company> request = new HttpEntity<>(o);
        final ResponseEntity<Company> response = rest.exchange(url, POST, request, Company.class);
        assertEquals(OK, response.getStatusCode());

        return response.getBody();
    }
}