package com.piercey.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piercey.models.Action;
import com.piercey.models.Permission;
import com.piercey.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoleControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @BeforeEach
    public void setup() {
        // We need to swap out the default object mapper for our own so we can configure how enums are converted
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        rest.getRestTemplate().getMessageConverters().removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
        rest.getRestTemplate().getMessageConverters().add(converter);
    }

    @Test
    void createAndDeleteRole() {
        HttpEntity<Role> createRequest = new HttpEntity<>(
                new Role("zzz", "0192204e-1fdd-705b-9263-ff0aa4277ad9")
                        .setId("0192219a-5b3a-7b15-9677-24e25c5298fa")
                        .addPermission(new Permission("aaa")
                                .addAllActions()));

        ResponseEntity<Role> createResponse = rest.exchange(
                String.format("http://localhost:%d/api/role", port),
                HttpMethod.POST,
                createRequest,
                Role.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertEquals("zzz", createResponse.getBody().getRoleName());
        assertEquals("0192219a-5b3a-7b15-9677-24e25c5298fa", createResponse.getBody().getId());
        assertEquals("0192204e-1fdd-705b-9263-ff0aa4277ad9", createResponse.getBody().getCompanyId());
        assertTrue(createResponse.getBody().hasPermission("aaa"));
        assertTrue(createResponse.getBody().hasPermissionWith("aaa", Action.Create));

        HttpEntity<Void> deleteRequest = new HttpEntity<>(null);

        ResponseEntity<Void> deleteResponse = rest.exchange(
                String.format("http://localhost:%d/api/role/0192204e-1fdd-705b-9263-ff0aa4277ad9", port),
                HttpMethod.DELETE,
                deleteRequest,
                Void.class);

        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
    }

    @Test
    void findAllRoles() {
        HttpEntity<Void> request = new HttpEntity<>(null);

        ResponseEntity<Role[]> response = rest.exchange(
                String.format("http://localhost:%d/api/roles", port),
                HttpMethod.GET,
                request,
                Role[].class);

        List<Role> roles = List.of(Objects.requireNonNull(response.getBody()));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(roles.stream().anyMatch(o -> "Admin".equals(o.getRoleName())));
        assertTrue(roles.stream().anyMatch(o -> "Normal User".equals(o.getRoleName())));
        assertTrue(roles.stream().anyMatch(o -> "0192204e-1fdd-705b-9263-ff0aa4277ad9".equals(o.getCompanyId())));
    }

    @Test
    void findRolesByCompanyId() {
        HttpEntity<Void> request = new HttpEntity<>(null);

        ResponseEntity<Role[]> response = rest.exchange(
                String.format("http://localhost:%d/api/roles?companyId=0192204e-1fdd-705b-9263-ff0aa4277ad9", port),
                HttpMethod.GET,
                request,
                Role[].class);

        List<Role> roles = List.of(Objects.requireNonNull(response.getBody()));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(roles.stream().allMatch(o -> "0192204e-1fdd-705b-9263-ff0aa4277ad9".equals(o.getCompanyId())));
    }

    @Test
    void findRolesByCompanyIdAndUserId() {
        HttpEntity<Void> request = new HttpEntity<>(null);

        ResponseEntity<Role[]> response = rest.exchange(
                String.format("http://localhost:%d/api/roles?companyId=0192204e-1fdd-705b-9263-ff0aa4277ad9&userId=0191c825-3a39-75d2-a90f-8e9bbde70698", port),
                HttpMethod.GET,
                request,
                Role[].class);

        List<Role> roles = List.of(Objects.requireNonNull(response.getBody()));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().length);
        assertTrue(roles.stream().allMatch(o -> "0192204e-1fdd-705b-9263-ff0aa4277ad9".equals(o.getCompanyId())));
    }

    @Test
    void findRoleByRoleId() {
        HttpEntity<Void> request = new HttpEntity<>(null);

        ResponseEntity<Role> response = rest.exchange(
                String.format("http://localhost:%d/api/role/0192204c-e839-7f74-8ed8-432e89e25763", port),
                HttpMethod.GET,
                request,
                Role.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("0192204c-e839-7f74-8ed8-432e89e25763", response.getBody().getId());
        assertEquals("0192204e-1fdd-705b-9263-ff0aa4277ad9", response.getBody().getCompanyId());
        assertEquals("Normal User", response.getBody().getRoleName());
    }

    @Test
    void findRolesByCompanyIdAndRoleName() {
        HttpEntity<Void> request = new HttpEntity<>(null);

        ResponseEntity<Role[]> response = rest.exchange(
                String.format("http://localhost:%d/api/roles?companyId=0192204e-1fdd-705b-9263-ff0aa4277ad9&roleName=Normal+User", port),
                HttpMethod.GET,
                request,
                Role[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals("0192204c-e839-7f74-8ed8-432e89e25763", response.getBody()[0].getId());
        assertEquals("0192204e-1fdd-705b-9263-ff0aa4277ad9", response.getBody()[0].getCompanyId());
        assertEquals("Normal User", response.getBody()[0].getRoleName());
    }

    @Test
    void updateRole() {
        HttpEntity<Role> request = new HttpEntity<>(
                new Role("qqq", "0192204e-1fdd-705b-9263-ff0aa4277ad9")
                        .setId("0192219a-5b3a-7b15-9677-24e25c5298fa")
                        .addPermission(new Permission("www")
                                .addAllActions()));

        ResponseEntity<Role> response = rest.exchange(
                String.format("http://localhost:%d/api/role", port),
                HttpMethod.POST,
                request,
                Role.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("qqq", response.getBody().getRoleName());
        assertEquals("0192219a-5b3a-7b15-9677-24e25c5298fa", response.getBody().getId());
        assertEquals("0192204e-1fdd-705b-9263-ff0aa4277ad9", response.getBody().getCompanyId());

        request = new HttpEntity<>(
                new Role("sss", "0192204e-1fdd-705b-9263-ff0aa4277ad9"));

        response = rest.exchange(
                String.format("http://localhost:%d/api/role/0192219a-5b3a-7b15-9677-24e25c5298fa", port),
                HttpMethod.PUT,
                request,
                Role.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("sss", response.getBody().getRoleName());
        assertEquals("0192219a-5b3a-7b15-9677-24e25c5298fa", response.getBody().getId());
        assertEquals("0192204e-1fdd-705b-9263-ff0aa4277ad9", response.getBody().getCompanyId());
    }
}