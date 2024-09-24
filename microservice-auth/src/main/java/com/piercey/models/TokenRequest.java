package com.piercey.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenRequest {
    private @JsonProperty("username") String username;
    private @JsonProperty("password") String password;
    private @JsonProperty("refresh_token") String refreshToken;

    public TokenRequest() {
    }

    public String getUsername() {
        return !Strings.isNullOrEmpty(username) ? username : "";
    }

    public TokenRequest setUsername(String username) {
        this.username = !Strings.isNullOrEmpty(username) ? username : "";
        return this;
    }

    public String getPassword() {
        return !Strings.isNullOrEmpty(password) ? password : "";
    }

    public TokenRequest setPassword(String password) {
        this.password = !Strings.isNullOrEmpty(password) ? password : "";
        return this;
    }

    public String getRefreshToken() {
        return !Strings.isNullOrEmpty(refreshToken) ? refreshToken : "";
    }

    public TokenRequest setRefreshToken(String refreshToken) {
        this.refreshToken = !Strings.isNullOrEmpty(refreshToken) ? refreshToken : "";
        return this;
    }
}
