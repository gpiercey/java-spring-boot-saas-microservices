package com.piercey.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.springframework.lang.NonNull;

import java.util.*;

@JsonRootName("Permission")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission {
    private String resource;
    private final Set<Action> actions = new HashSet<>();

    public Permission() {
    }

    public Permission(@NonNull String resource) {
        this.resource = resource;
    }

    public Permission(@NonNull String resource, @NonNull List<Action> actions) {
        this.resource = resource;
        this.actions.addAll(actions);
    }

    public String getResource() {
        return resource;
    }

    public Permission setResource(@NonNull String resource) {
        this.resource = resource;
        return this;
    }

    public Set<Action> getActions() {
        return actions;
    }

    public Permission setActions(@NonNull Set<Action> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
        return this;
    }

    public Permission addAction(@NonNull Action action) {
        actions.add(action);
        return this;
    }

    public Permission addActions(@NonNull Set<Action> actions) {
        this.actions.addAll(actions);
        return this;
    }

    public Permission addAllActions() {
        this.actions.addAll(Arrays.stream(Action.values()).toList());
        return this;
    }

    public Permission removeAction(@NonNull Action action) {
        actions.remove(action);
        return this;
    }

    public boolean hasAction(@NonNull Action action) {
        return actions.contains(action);
    }

    public boolean hasAllActions() {
        return actions.containsAll(Arrays.stream(Action.values()).toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(resource, that.resource) && Objects.equals(actions, that.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, actions);
    }

    @Override
    public String toString() {
        return "Permission{" +
                "name='" + resource + '\'' +
                ", actions=" + actions +
                '}';
    }
}