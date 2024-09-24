package com.piercey.repositories;

import com.piercey.models.UserRole;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends MongoRepository<UserRole, String> {

    @Query("{userId:'?0'}")
    List<UserRole> findRolesByUserId(String userId);

    @DeleteQuery("{roleId:'?0'}")
    void deleteRoleByRoleId(String roleId);
}