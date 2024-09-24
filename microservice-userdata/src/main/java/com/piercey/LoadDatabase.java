package com.piercey;

import com.piercey.models.UserData;
import com.piercey.models.UserStatus;
import com.piercey.repositories.UserDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.List;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initUserDatabase(final @NonNull UserDataRepository repository) {
        final List<UserData> users = Arrays.asList(
                new UserData("0191c825-3a39-706e-abef-17b7ca1027f5", "admin").setStatus(UserStatus.ACTIVE),       // Password123!
                new UserData("0191c825-3a39-75d2-a90f-8e9bbde70698", "gpiercey").setStatus(UserStatus.ACTIVE),    // Password123!
                new UserData("0191c825-3a39-7869-8b36-27827431f6e2", "auth-svc").setStatus(UserStatus.ACTIVE),    // Password123!
                new UserData("0191c825-3a39-75a3-bd38-e6eda138b09b", "user-svc").setStatus(UserStatus.ACTIVE),    // Password123!
                new UserData("0191c825-3a39-7a5a-9668-ebd45a450691", "company-svc").setStatus(UserStatus.ACTIVE), // Password123!
                new UserData("0191c825-3a39-7316-a092-b9452d2a8a27", "ismith").setStatus(UserStatus.ACTIVE),      // Password123!
                new UserData("0191c825-3a39-7576-a017-9276b3d3fc4b", "psharp").setStatus(UserStatus.ACTIVE),      // Password123!
                new UserData("0191c825-3a39-7b29-ae9a-2a3c9145e76a", "ccross").setStatus(UserStatus.PENDING),     // Password123!
                new UserData("0191c825-3a39-7e20-a900-c36edc262cb0", "rgosselin").setStatus(UserStatus.SUSPENDED),// Password123!
                new UserData("0191c825-3a39-760c-b66d-b37d7c647134", "hcarson").setStatus(UserStatus.ARCHIVED)    // Password123!
        );

        return args -> {
            users.forEach(o -> log.info("Preloading {}", repository.save(o)));
        };
    }
}