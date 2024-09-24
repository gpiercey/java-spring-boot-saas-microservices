package com.piercey.repositories;

import com.piercey.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {

    @Query("{id:?0}")
    Role findByRoleId(String roleId);

    @Query("{roleName:'?0', companyId:'?1'}")
    Role findByNameAndCompanyId(String roleName, String companyId);

    @Query("{companyId:'?0'}")
    List<Role> findByCompanyId(String companyId);
}