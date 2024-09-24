package com.piercey.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.piercey.models.UserData;
import com.piercey.models.UserStatus;
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
class UserDataControllerTest {
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
        UserData o = new UserData()
                .setId(UUID.randomUUID().toString())
                .setStatus(UserStatus.PENDING)
                .setUsername("zzz")
                .setEmail("zzz@aaa.com")
                .setFullname("Blah Blah Blah");

        UserData response = postObject(o);
        assertEquals(o.getId(), response.getId());
        assertEquals(o.getUsername(), response.getUsername());
        assertEquals(o.getEmail(), response.getEmail());
        assertEquals(o.getFullname(), response.getFullname());
        assertEquals(UserStatus.ACTIVE.toString(), response.getStatus());
    }

    @Test
    void createOneWithCSSVulnerability() {
        UserData o = new UserData()
                .setId(UUID.randomUUID().toString())
                .setStatus(UserStatus.PENDING)
                .setUsername("zzz")
                .setEmail("zzz@aaa.com")
                .setFullname("Blah Blah Blah<script></script>");

        UserData response = postObject(o);
        assertEquals(o.getId(), response.getId());
        assertEquals(o.getUsername(), response.getUsername());
        assertEquals(o.getEmail(), response.getEmail());
        assertEquals("Blah Blah Blah", response.getFullname()); // check that this has been scrubbed
        assertEquals(UserStatus.ACTIVE.toString(), response.getStatus());
    }

    @Test
    void readAll() {
        String url = String.format("http://localhost:%d/api/users", port);

        HttpEntity<Void> request = new HttpEntity<>(null);
        ResponseEntity<UserData[]> response = rest.exchange(url, GET, request, UserData[].class);
        assertEquals(OK, response.getStatusCode());

        List<UserData> users = List.of(Objects.requireNonNull(response.getBody()));
        assertTrue(users.stream().anyMatch(o -> "gpiercey".equals(o.getUsername())));
        assertTrue(users.stream().anyMatch(o -> "admin".equals(o.getUsername())));
    }

    @Test
    void readOne() {
        String url = String.format("http://localhost:%d/api/user/0191c825-3a39-75d2-a90f-8e9bbde70698", port); // gpiercey
        HttpEntity<Void> request = new HttpEntity<>(null);

        ResponseEntity<UserData> response = rest.exchange(url, GET, request, UserData.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals("gpiercey", response.getBody().getUsername());
    }

    @Test
    void updateOne() {
        String url = String.format("http://localhost:%d/api/user/0191c825-3a39-75d2-a90f-8e9bbde70698", port); // gpiercey

        HttpEntity<Void> request = new HttpEntity<>(null);
        ResponseEntity<UserData> response = rest.exchange(url, GET, request, UserData.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals("gpiercey", response.getBody().getUsername());

        HttpEntity<UserData> updateRequest = new HttpEntity<>(response.getBody().setFullname("Blah Blah Blah"));
        ResponseEntity<UserData> updateResponse = rest.exchange(url, PUT, updateRequest, UserData.class);
        assertEquals(OK, updateResponse.getStatusCode());
        assertEquals("gpiercey", updateResponse.getBody().getUsername());
        assertEquals("Blah Blah Blah", updateResponse.getBody().getFullname());

        response = rest.exchange(url, GET, request, UserData.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals("gpiercey", response.getBody().getUsername());
        assertEquals("Blah Blah Blah", response.getBody().getFullname());
    }

    @Test
    void updateOneWithCSSVulnerability() {
        String url = String.format("http://localhost:%d/api/user/0191c825-3a39-75d2-a90f-8e9bbde70698", port); // gpiercey

        HttpEntity<Void> request = new HttpEntity<>(null);
        ResponseEntity<UserData> response = rest.exchange(url, GET, request, UserData.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals("gpiercey", response.getBody().getUsername());

        HttpEntity<UserData> updateRequest = new HttpEntity<>(response.getBody().setFullname("Blah Blah Blah<script></script>"));
        ResponseEntity<UserData> updateResponse = rest.exchange(url, PUT, updateRequest, UserData.class);
        assertEquals(OK, updateResponse.getStatusCode());
        assertEquals("gpiercey", updateResponse.getBody().getUsername());
        assertEquals("Blah Blah Blah", updateResponse.getBody().getFullname()); // ensure it was scrubbed

        response = rest.exchange(url, GET, request, UserData.class);
        assertEquals(OK, response.getStatusCode());
        assertEquals("gpiercey", response.getBody().getUsername());
        assertEquals("Blah Blah Blah", response.getBody().getFullname());  // ensure it was scrubbed
    }

    @Test
    void deleteUSerData() {
        String url = String.format("http://localhost:%d/api/user/0191c825-3a39-75d2-a90f-8e9bbde70698", port); // gpiercey

        HttpEntity<Void> request = new HttpEntity<>(null);
        ResponseEntity<Void> response = rest.exchange(url, DELETE, request, Void.class);
        assertEquals(OK, response.getStatusCode());

        ResponseEntity<UserData> updateResponse = rest.exchange(url, GET, request, UserData.class);
        assertEquals(OK, updateResponse.getStatusCode());
        assertEquals(UserStatus.ARCHIVED.toString(), updateResponse.getBody().getStatus());
    }

    private UserData postObject(final @NonNull UserData o) {
        final String url = String.format("http://localhost:%d/api/user", port);

        final HttpEntity<UserData> request = new HttpEntity<>(o);
        final ResponseEntity<UserData> response = rest.exchange(url, POST, request, UserData.class);
        assertEquals(OK, response.getStatusCode());

        return response.getBody();
    }
}