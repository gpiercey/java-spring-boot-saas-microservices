package com.piercey.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Document(collection = "roles")
public class Role {
    @Id
    private String id;

    private String roleName;

    private String companyId;

    private final Set<Permission> permissions = new HashSet<>();

    public Role() {
    }

    public Role(String roleName, String companyId) {
        this.roleName = roleName;
        this.companyId = companyId;
    }

    public Role(String roleName, String companyId, Set<Permission> permissions) {
        this.roleName = roleName;
        this.companyId = companyId;
        this.permissions.addAll(permissions);
    }

    public Role(String admin) {
    }

    public String getId() {
        return id;
    }

    public Role setId(String id) {
        this.id = id;
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public Role setRoleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    public String getCompanyId() {
        return companyId;
    }

    public Role setCompanyId(String companyId) {
        this.companyId = companyId;
        return this;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public Role setPermissions(Set<Permission> permissions) {
        this.permissions.clear();
        this.permissions.addAll(permissions);
        return this;
    }

    public Role addPermission(Permission permission) {
        permissions.add(permission);
        return this;
    }

    public Role addPermissions(Set<Permission> permissions) {
        this.permissions.addAll(permissions);
        return this;
    }

    public Role removePermission(Permission permission) {
        permissions.remove(permission);
        return this;
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                .anyMatch(o -> permissionName.compareToIgnoreCase(o.getResource()) == 0);
    }

    public boolean hasPermissionWith(String permissionName, Action action) {
        return permissions.stream()
                .anyMatch(o -> permissionName.compareToIgnoreCase(o.getResource()) == 0 && o.hasAction(action));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(roleName, role.roleName) && Objects.equals(companyId, role.companyId) && Objects.equals(permissions, role.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleName, companyId, permissions);
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleName='" + roleName + '\'' +
                ", companyId='" + companyId + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}