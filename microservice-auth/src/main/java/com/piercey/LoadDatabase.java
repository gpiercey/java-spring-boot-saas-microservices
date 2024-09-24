package com.piercey;

import com.piercey.models.Permission;
import com.piercey.models.Role;
import com.piercey.models.UserRole;
import com.piercey.repositories.RoleRepository;
import com.piercey.repositories.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initRolesCollection(RoleRepository repository) {

        final String companyId = "0192204e-1fdd-705b-9263-ff0aa4277ad9";

        final List<Role> roles = Arrays.asList(
                new Role("Admin", companyId).setId("0192204c-e839-7d0a-95da-2565baa55a15") // admin
                        .addPermission(new Permission("Users").addAllActions())
                        .addPermission(new Permission("Data").addAllActions())
                        .addPermission(new Permission("Documents").addAllActions()),
                new Role("Normal User", companyId).setId("0192204c-e839-7f74-8ed8-432e89e25763") // gpiercey
                        .addPermission(new Permission("Data").addAllActions())
                        .addPermission(new Permission("Documents").addAllActions())
        );

        return args -> {
            roles.forEach(role -> log.info("Preloading {}", repository.save(role)));
        };
    }

    @Bean
    CommandLineRunner initUserRolesCollection(UserRoleRepository repository) {

        final List<UserRole> associations = Arrays.asList(
                new UserRole("0191c825-3a39-706e-abef-17b7ca1027f5", "0192204c-e839-7d0a-95da-2565baa55a15"), // admin
                new UserRole("0191c825-3a39-75d2-a90f-8e9bbde70698", "0192204c-e839-7f74-8ed8-432e89e25763"), // gpiercey
                new UserRole("0191c825-3a39-7869-8b36-27827431f6e2", "0192204c-e839-7f74-8ed8-432e89e25763"), // jlowe
                new UserRole("0191c825-3a39-75a3-bd38-e6eda138b09b", "0192204c-e839-7f74-8ed8-432e89e25763"), // bdagenais
                new UserRole("0191c825-3a39-7a5a-9668-ebd45a450691", "0192204c-e839-7f74-8ed8-432e89e25763"), // yfunk
                new UserRole("0191c825-3a39-7316-a092-b9452d2a8a27", "0192204c-e839-7f74-8ed8-432e89e25763"), // ismith
                new UserRole("0191c825-3a39-7576-a017-9276b3d3fc4b", "0192204c-e839-7f74-8ed8-432e89e25763"), // psharp
                new UserRole("0191c825-3a39-7b29-ae9a-2a3c9145e76a", "0192204c-e839-7f74-8ed8-432e89e25763"), // ccross
                new UserRole("0191c825-3a39-7e20-a900-c36edc262cb0", "0192204c-e839-7f74-8ed8-432e89e25763"), // rgosselin
                new UserRole("0191c825-3a39-760c-b66d-b37d7c647134", "0192204c-e839-7f74-8ed8-432e89e25763")  // hcarson
        );

        return args -> {
            associations.forEach(o -> log.info("Preloading {}", repository.save(o)));
        };
    }
}