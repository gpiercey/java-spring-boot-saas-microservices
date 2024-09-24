package com.piercey.repositories;

import com.piercey.models.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends MongoRepository<Company, String> {

    @Query("{companyName:'?0'}")
    Company findByCompanyName(final @NonNull String companyName);
}