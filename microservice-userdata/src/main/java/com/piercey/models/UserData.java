package com.piercey.models;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Document(collection = "user_data")
public class UserData implements Serializable {
    private @Id String id;
    private @CreatedDate Date created = new Date();
    private @LastModifiedDate Date modified = new Date();
    private String username;
    private String email;
    private String companyId;
    private String fullname;
    private String status;

    public UserData() {
    }

    public UserData(final @NonNull String id, final @NonNull String username) {
        this.id = id;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public UserData setId(final @NonNull String id) {
        this.id = id;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public UserData setCreated(final @NonNull Date created) {
        this.created = created;
        return this;
    }

    public Date getModified() {
        return modified;
    }

    public UserData setModified(final @NonNull Date modified) {
        this.modified = modified;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserData setUsername(final @NonNull String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserData setEmail(final @NonNull String email) {
        this.email = email;
        return this;
    }

    public String getFullname() {
        return fullname;
    }

    public UserData setFullname(final @NonNull String fullname) {
        this.fullname = fullname;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserData setStatus(final @NonNull String status) {
        this.status = status;
        return this;
    }

    public UserData setStatus(final @NonNull UserStatus status) {
        this.status = status.toString();
        return this;
    }

    public String getCompanyId() {
        return companyId;
    }

    public UserData setCompanyId(final @NonNull String companyId) {
        this.companyId = companyId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserData userData = (UserData) o;
        return Objects.equals(id, userData.id) && Objects.equals(created, userData.created) && Objects.equals(modified, userData.modified) && Objects.equals(username, userData.username) && Objects.equals(email, userData.email) && Objects.equals(fullname, userData.fullname) && Objects.equals(status, userData.status) && Objects.equals(companyId, userData.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, created, modified, username, email, fullname, status, companyId);
    }

    @Override
    public String toString() {
        return "UserData{" +
                "id='" + id + '\'' +
                ", created=" + created +
                ", modified=" + modified +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullname='" + fullname + '\'' +
                ", status='" + status + '\'' +
                ", companyId='" + companyId + '\'' +
                '}';
    }
}