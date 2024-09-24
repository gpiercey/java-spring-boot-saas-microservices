package com.piercey.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.piercey.exceptions.Http401Exception;
import com.piercey.exceptions.HttpException;
import com.piercey.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

// Security Warning!
// It is highly recommended that you use an external identity provider (IDP) or directory service (DS) to store user
// credentials, store those without PII and make sure it is completely isolated from your central database,
// so in the event of a breach there is no way to easily tie hashed passwords to a username or email. User data
// should be stored in your central database without credentials, the only referential data point would be the
// user id.
//
// This service will authenticate against the IDP/DS and will generate + sign tokens itself for better control of
// performance and uptime, and reduce impacts of intermittent network problems on the WAN. It should utilize
// caching to lower latency and reduce the number of callbacks it makes to the IDP/DS. It should validate tokens
// for all internal microservices so it doesn't have to share its crypto keys with any other services, and it
// should operate with multiple replicas, load balanced and have an aggressive autoscaling policy. Authentication
// and authorization is the single most critical piece of a SaaS application so we need to expect it to be under
// heavy load with a high risk of attack and should be built to sustain an uptime in the scale of 99.999% or greater.

// Note:
// This class should be an interface for the integration with an external Identity Provider (IDP) or Directory
// Service (DS). For the purposes of demonstration I'm just going to hard code some user data but if you decide
// to adapt this to a production scenario you should complete the integration.

@Component
public class ExternalIdentityProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ObjectMapper objectMapper;

    private final List<User> users = Arrays.asList(
            new User("0191c825-3a39-706e-abef-17b7ca1027f5", "Password123!"), // admin
            new User("0191c825-3a39-75d2-a90f-8e9bbde70698", "Password123!"), // gpiercey
            new User("0191c825-3a39-7869-8b36-27827431f6e2", "Password123!"), // jlowe (random)
            new User("0191c825-3a39-75a3-bd38-e6eda138b09b", "Password123!"), // bdagenais (random)
            new User("0191c825-3a39-7a5a-9668-ebd45a450691", "Password123!"), // yfunk (random)
            new User("0191c825-3a39-7316-a092-b9452d2a8a27", "Password123!"), // ismith (random)
            new User("0191c825-3a39-7576-a017-9276b3d3fc4b", "Password123!"), // psharp (random)
            new User("0191c825-3a39-7b29-ae9a-2a3c9145e76a", "Password123!"), // ccross (random)
            new User("0191c825-3a39-7e20-a900-c36edc262cb0", "Password123!"), // rgosselin (random)
            new User("0191c825-3a39-760c-b66d-b37d7c647134", "Password123!")  // hcarson (random)
    );

    public String authenticate(@NonNull String userId, @NonNull String password) {
        // TODO: This is a good place to start your integration with an IDP/DS
        // {
        final User user = users.stream()
                .filter(o -> userId.compareToIgnoreCase(o.getId().toString()) == 0)
                .findFirst()
                .orElse(null);

        if (user == null) {
            logger.warn("authentication failed: id not found for user");
            throw new Http401Exception("invalid id or password");
        }

        if (!user.compareWithPassword(password, false)) {
            logger.warn("authentication failed: invalid password for {}", userId);
            throw new Http401Exception("invalid userId or password");
        }
        // }

        return user.getId().toString();
    }

    public String authenticate(@NonNull String refreshToken) throws HttpException {
        final String id = extractUidFromToken(refreshToken);

        if (Strings.isNullOrEmpty(id)) {
            logger.warn("authentication failed: id not found in refresh token");
            throw new Http401Exception("invalid refresh token");
        }

        final User user = users.stream()
                .filter(o -> id.compareTo(o.getId().toString()) == 0)
                .findFirst()
                .orElse(null);

        if (user == null) {
            logger.warn("authentication failed: id from refresh token not found {}", id);
            throw new Http401Exception("invalid refresh token");
        }

        return user.getId().toString();
    }

    private String extractUidFromToken(@NonNull String token) {
        try {
            final String content = new String(Base64.getUrlDecoder()
                    .decode(token.split("\\.")[1]));

            return objectMapper.readTree(content)
                    .at("/sub")
                    .asText("");

        } catch (Exception e) {
            return "";
        }
    }
}