package com.piercey.models;

import com.google.common.hash.Hashing;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class User {
    private UUID id;
    private String passwordHash;

    User() {
    }

    public User(@NonNull UUID id, @NonNull String password) {
        this.id = id;
        passwordHash = generateHash(password);
    }

    public User(@NonNull String id, @NonNull String password) {
        this.id = UUID.fromString(id);
        passwordHash = generateHash(password);
    }

    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(@NonNull String password) {
        passwordHash = generateHash(password);
    }

    public boolean compareWithPassword(@NonNull String password, boolean plainText) {
        return plainText
                ? passwordHash.equals(generateHash(password))
                : passwordHash.equals(password);
    }

    private String generateHash(@NonNull String s) {
        return Hashing.sha256()
                .hashString(s, StandardCharsets.UTF_8)
                .toString();
    }
}