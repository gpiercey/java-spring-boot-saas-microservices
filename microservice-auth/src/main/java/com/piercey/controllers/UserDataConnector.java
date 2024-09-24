package com.piercey.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.piercey.exceptions.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserDataConnector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${microservice.userdata.host:localhost}")
    private String userdataHost;

    @Value("${microservice.userdata.port:8002}")
    private long userdataPort;

    public JsonNode findByUserId(final @NonNull String id) {
        final HttpEntity<Void> request = new HttpEntity<>(null);

        final ResponseEntity<JsonNode> response = restTemplate.exchange(
                String.format("http://%s:%d/api/user/%s", userdataHost, userdataPort, id),
                HttpMethod.GET,
                request,
                JsonNode.class);

        if (response.getStatusCode().isError()) {
            throw new HttpException(
                    response.getStatusCode().value(),
                    response.getStatusCode().toString());
        }

        return response.getBody() == null
                ? JsonNodeFactory.instance.objectNode()
                : response.getBody();
    }

    public JsonNode findByUsername(final @NonNull String username) {
        final HttpEntity<Void> request = new HttpEntity<>(null);

        // TODO: generate a token and add to the authorization header

        final ResponseEntity<JsonNode> response = restTemplate.exchange(
                String.format("http://%s:%d/api/users?username=%s", userdataHost, userdataPort, username),
                HttpMethod.GET,
                request,
                JsonNode.class);

        if (response.getStatusCode().isError()) {
            throw new HttpException(
                    response.getStatusCode().value(),
                    response.getStatusCode().toString());
        }

        return response.getBody() == null
                ? JsonNodeFactory.instance.objectNode()
                : response.getBody();
    }
}