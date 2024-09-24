package com.piercey.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "user_roles")
public class UserRole {

    @Id
    private String id;

    private String userId;

    private String roleId;

    public UserRole() {
    }

    public UserRole(String userId, String roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public UserRole(String id, String userId, String roleId) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
    }

    public String getId() {
        return id;
    }

    public UserRole setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public UserRole setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getRoleId() {
        return roleId;
    }

    public UserRole setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRole userRole = (UserRole) o;
        return Objects.equals(id, userRole.id) && Objects.equals(userId, userRole.userId) && Objects.equals(roleId, userRole.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, roleId);
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", roleId='" + roleId + '\'' +
                '}';
    }
}