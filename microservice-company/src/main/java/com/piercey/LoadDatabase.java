package com.piercey;

import com.piercey.models.Company;
import com.piercey.models.CompanyStatus;
import com.piercey.repositories.CompanyRepository;
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
    CommandLineRunner initUserDatabase(final @NonNull CompanyRepository repository) {
        final List<Company> users = Arrays.asList(
                new Company("01920ce1-4a4b-7677-a9b4-51f74af864f9", "ABC").setStatus(CompanyStatus.ACTIVE),
                new Company("01920ce1-4a4b-7ea8-8ac6-21ecbf4d4e93", "DEF").setStatus(CompanyStatus.ACTIVE),
                new Company("01920ce1-4a4b-7315-a10a-f429b613f9c6", "GHI").setStatus(CompanyStatus.ACTIVE),
                new Company("01920ce1-4a4b-7325-9cc9-2d6a0bc54185", "JKL").setStatus(CompanyStatus.ACTIVE),
                new Company("01920ce1-4a4b-7804-be75-dfad5cb1cbef", "MNO").setStatus(CompanyStatus.ACTIVE)
        );

        return args -> {
            users.forEach(o -> log.info("Preloading {}", repository.save(o)));
        };
    }
}