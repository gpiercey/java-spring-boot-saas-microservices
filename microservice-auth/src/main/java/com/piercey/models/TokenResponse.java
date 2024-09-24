package com.piercey.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {
    private @JsonProperty("access_token") String accessToken;
    private @JsonProperty("refresh_token") String refreshToken;

    public TokenResponse() {
    }

    public TokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public TokenResponse setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public TokenResponse setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }
}
