package com.piercey.repositories;

import com.piercey.models.UserData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDataRepository extends MongoRepository<UserData, String> {

    @Query("{'username':?0}")
    UserData findByUsername(final @NonNull String username);

    @Query("{'email':?0}")
    UserData findByEmail(final @NonNull String email);

    @Query("{'companyId':?0}")
    List<UserData> findByCompanyId(final @NonNull String companyId);
}