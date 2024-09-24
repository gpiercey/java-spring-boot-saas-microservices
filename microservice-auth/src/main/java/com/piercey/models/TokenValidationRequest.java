package com.piercey.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenValidationRequest {
    private @JsonProperty("access_token") String accessToken;
    private @JsonProperty("user_id") String userId;

    public TokenValidationRequest() {
    }

    public String getAccessToken() {
        return !Strings.isNullOrEmpty(accessToken) ? accessToken : "";
    }

    public TokenValidationRequest setAccessToken(String accessToken) {
        this.accessToken = !Strings.isNullOrEmpty(accessToken) ? accessToken : "";
        return this;
    }

    public String getUserId() {
        return !Strings.isNullOrEmpty(userId) ? userId : "";
    }

    public TokenValidationRequest setUserId(String userId) {
        this.userId = !Strings.isNullOrEmpty(userId) ? userId : "";
        return this;
    }
}