package com.piercey.models;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Document(collection = "company")
public class Company implements Serializable {
    private @Id String id;
    private @CreatedDate Date created = new Date();
    private @LastModifiedDate Date modified = new Date();
    private String companyName;
    private String status;

    public Company() {
    }

    public Company(final @NonNull String id, final @NonNull String companyName) {
        this.id = id;
        this.companyName = companyName;
    }

    public String getId() {
        return id;
    }

    public Company setId(final @NonNull String id) {
        this.id = id;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public Company setCreated(final @NonNull Date created) {
        this.created = created;
        return this;
    }

    public Date getModified() {
        return modified;
    }

    public Company setModified(final @NonNull Date modified) {
        this.modified = modified;
        return this;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Company setCompanyName(final @NonNull String companyName) {
        this.companyName = companyName;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Company setStatus(final @NonNull String status) {
        this.status = status;
        return this;
    }

    public Company setStatus(final @NonNull CompanyStatus status) {
        this.status = status.toString();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return Objects.equals(id, company.id) && Objects.equals(created, company.created) && Objects.equals(modified, company.modified) && Objects.equals(companyName, company.companyName) && Objects.equals(status, company.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, created, modified, companyName, status);
    }

    @Override
    public String toString() {
        return "Company{" +
                "id='" + id + '\'' +
                ", created=" + created +
                ", modified=" + modified +
                ", companyName='" + companyName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}