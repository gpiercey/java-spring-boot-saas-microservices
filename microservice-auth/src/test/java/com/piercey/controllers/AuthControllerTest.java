package com.piercey.controllers;

import com.google.common.base.Strings;
import com.piercey.models.TokenRequest;
import com.piercey.models.TokenResponse;
import com.piercey.models.TokenValidationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void acquireTokenUsingValidCredentials() {
        String[] tokens = acquireTokens("gpiercey", "Password123!", HttpStatus.OK);
        assertEquals(2, tokens.length);
        assertFalse(Strings.isNullOrEmpty(tokens[0])); // access token
        assertFalse(Strings.isNullOrEmpty(tokens[1])); // refresh token
    }

    @Test
    void acquireTokenUsingInvalidCredentials() {
        acquireTokens("gpiercey", "zzz", HttpStatus.UNAUTHORIZED);
        acquireTokens("gpiercey", "Password123", HttpStatus.UNAUTHORIZED);
        acquireTokens("gpiercey", "password123!", HttpStatus.UNAUTHORIZED);
        acquireTokens("zzz", "Password123!", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void acquireTokenUsingValidRefreshToken() {
        String[] tokens = acquireTokens("gpiercey", "Password123!", HttpStatus.OK);
        assertEquals(2, tokens.length);
        assertFalse(Strings.isNullOrEmpty(tokens[0])); // access token
        assertFalse(Strings.isNullOrEmpty(tokens[1])); // refresh token

        tokens = acquireTokens(tokens[1], HttpStatus.OK);
        assertEquals(2, tokens.length);
        assertFalse(Strings.isNullOrEmpty(tokens[0])); // access token
        assertFalse(Strings.isNullOrEmpty(tokens[1])); // refresh token
    }

    @Test
    void acquireTokenUsingInvalidRefreshToken() {
        acquireTokens("zzz.zzz.zzz", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validateValidToken() {
        String[] tokens = acquireTokens("gpiercey", "Password123!", HttpStatus.OK);
        assertEquals(2, tokens.length);
        assertFalse(Strings.isNullOrEmpty(tokens[0])); // access token
        assertFalse(Strings.isNullOrEmpty(tokens[1])); // refresh token
        validateToken(tokens[0], HttpStatus.OK);
    }

    @Test
    void validateInvalidToken() {
        validateToken("zzz", HttpStatus.UNAUTHORIZED);
        validateToken("zzz.zzz.zzz", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logout() {
        String[] tokens = acquireTokens("gpiercey", "Password123!", HttpStatus.OK);
        assertEquals(2, tokens.length);
        assertFalse(Strings.isNullOrEmpty(tokens[0])); // access token
        assertFalse(Strings.isNullOrEmpty(tokens[1])); // refresh token
        validateToken(tokens[0], HttpStatus.OK);
        invalidateSelf(tokens[0], HttpStatus.OK);
        validateToken(tokens[0], HttpStatus.UNAUTHORIZED);
    }

    @Test
    void revokeToken() {
        String adminToken = acquireTokens("admin", "Password123!", HttpStatus.OK)[0];
        String adminUid = "0191c825-3a39-706e-abef-17b7ca1027f5";

        assertFalse(Strings.isNullOrEmpty(adminToken));
        validateToken(adminToken, HttpStatus.OK);

        String userToken = acquireTokens("gpiercey", "Password123!", HttpStatus.OK)[0];
        String userUid = "0191c825-3a39-75d2-a90f-8e9bbde70698";

        assertFalse(Strings.isNullOrEmpty(userToken));
        validateToken(userToken, HttpStatus.OK);

        invalidateOther(userToken, adminUid, HttpStatus.FORBIDDEN);
        invalidateOther(adminToken, userUid, HttpStatus.OK);
        validateToken(userToken, HttpStatus.UNAUTHORIZED);
    }

    private String[] acquireTokens(String username, String password, HttpStatus expectedResponse) {
        String url = String.format("http://localhost:%d/api/oauth/token", port);

        HttpEntity<TokenRequest> request = new HttpEntity<>(new TokenRequest()
                .setUsername(username)
                .setPassword(password));

        ResponseEntity<TokenResponse> response = rest.exchange(url, HttpMethod.POST, request, TokenResponse.class);
        assertSame(expectedResponse, response.getStatusCode());

        return new String[]{response.getBody().getAccessToken(), response.getBody().getRefreshToken()};
    }

    private String[] acquireTokens(String refreshToken, HttpStatus expectedResponse) {
        String url = String.format("http://localhost:%d/api/oauth/token", port);

        HttpEntity<TokenRequest> request = new HttpEntity<>(new TokenRequest()
                .setRefreshToken(refreshToken));

        ResponseEntity<TokenResponse> response = rest.exchange(url, HttpMethod.POST, request, TokenResponse.class);
        assertSame(expectedResponse, response.getStatusCode());

        return new String[]{response.getBody().getAccessToken(), response.getBody().getRefreshToken()};
    }

    private void validateToken(String token, HttpStatus expectedResponse) {
        String url = String.format("http://localhost:%d/api/oauth/validate", port);

        HttpEntity<TokenValidationRequest> request = new HttpEntity<>(new TokenValidationRequest()
                .setAccessToken(token));

        ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, request, Void.class);
        assertSame(expectedResponse, response.getStatusCode());
    }

    private void invalidateSelf(String token, HttpStatus expectedResponse) {
        String url = String.format("http://localhost:%d/api/oauth/logout", port);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer: " + token);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, request, Void.class);
        assertSame(expectedResponse, response.getStatusCode());
    }

    private void invalidateOther(String myToken, String otherUid, HttpStatus expectedResponse) {
        String url = String.format("http://localhost:%d/api/oauth/revoke", port);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer: " + myToken);

        HttpEntity<TokenValidationRequest> request = new HttpEntity<>(
                new TokenValidationRequest().setUserId(otherUid).setAccessToken(myToken),
                headers);

        ResponseEntity<Void> response = rest.exchange(url, HttpMethod.POST, request, Void.class);
        assertSame(expectedResponse, response.getStatusCode());
    }
}