package com.piercey.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.piercey.ExecutionTimer;
import com.piercey.exceptions.Http401Exception;
import com.piercey.exceptions.Http500Exception;
import com.piercey.external.ExternalIdentityProvider;
import com.piercey.models.Role;
import com.piercey.models.TokenRequest;
import com.piercey.models.TokenResponse;
import com.piercey.models.TokenValidationRequest;
import com.piercey.repositories.RoleRepository;
import com.piercey.repositories.UserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
public class AuthController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public enum TokenType {ACCESS, REFRESH}

    @Autowired
    private RedisCacheManager redisCacheManager;

    @Autowired
    private UserDataConnector userDataConnector;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Value("${token.lifespan.minutes:120}")
    private int tokenLifespan;

    @Value("${token.issuer:Undefined}")
    private String tokenIssuer;

    @Autowired
    private ExternalIdentityProvider idp;

    // TODO: it's better to not have this hard coded, use a KMS system instead, doing this for simplicity for demo.
    private final String tokenSecret = "jn&=S;z5s)XE9Pg<pNu(!M+Gmd}qT42tw#@*J6kWF[K.8,Ch$]";
    private final Algorithm algorithm = Algorithm.HMAC256(tokenSecret);

    // https://auth0.com/docs/authenticate/protocols/oauth#token-endpoint
    @PostMapping(value = "/api/oauth/token")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse acquireToken(
            final @RequestBody TokenRequest o,
            final @RequestHeader Map<String, String> headers,
            final HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            sanitizeUserInput(o, o.getClass());

            final String username = o.getUsername();
            final String password = generateHash(o.getPassword());
            final String refresh = o.getRefreshToken();

            if (refresh.isBlank()) {
                require(Strings.isNotBlank(username), 400, "username is required");
                require(Strings.isNotBlank(password), 400, "password is required");
            }

            if (username.isBlank()) {
                require(Strings.isNotBlank(refresh), 400, "refresh_token is required when credentials are absent");
            }

            if (!refresh.isBlank()) {
                validateToken(extractUidFromToken(refresh), refresh, TokenType.REFRESH);
            }

            final JsonNode subject = userDataConnector.findByUsername(username);
            require(subject.isArray() && subject.get(0) != null && !subject.get(0).at("/id").isNull(), 401, "failed to authenticate  " + username);

            final String userId = Strings.isBlank(refresh)
                    ? subject.get(0).at("/id").asText("")
                    : extractUidFromToken(refresh);

            final String userIdFromIdp = Strings.isBlank(refresh)
                    ? idp.authenticate(userId, password)
                    : idp.authenticate(refresh);

            require(userId.equals(userIdFromIdp), 401, "uid mismatch: " + userId + " != " + userIdFromIdp);

            final TokenResponse response = new TokenResponse()
                    .setAccessToken(generateToken(userId, TokenType.ACCESS))
                    .setRefreshToken(generateToken(userId, TokenType.REFRESH));

            redisCacheManager.getCache("auth-token")
                    .put(userId, JsonNodeFactory.instance.objectNode()
                            .put("access_token", generateHash(response.getAccessToken()))
                            .put("refresh_token", generateHash(response.getRefreshToken())));

            logger.info("generated token for user {}", userId);
            return response;
        }
    }

    @PostMapping(value = "/api/oauth/validate")
    @ResponseStatus(HttpStatus.OK)
    public void validateToken(
            final @RequestBody TokenValidationRequest o,
            final HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            sanitizeUserInput(o, o.getClass());

            final String token = o.getAccessToken();
            validateToken(extractUidFromToken(token), token, TokenType.ACCESS);
        }
    }

    @PostMapping(value = "/api/oauth/revoke")
    @ResponseStatus(HttpStatus.OK)
    public void revokeToken(
            final @RequestBody TokenValidationRequest o,
            final @RequestHeader Map<String, String> headers,
            final HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            sanitizeUserInput(o, o.getClass());

            final String userUid = o.getUserId();
            require(Strings.isNotBlank(userUid), 400, "user id is required");

            final String adminToken = extractTokenFromHeaders(headers);
            require(Strings.isNotBlank(adminToken), 400, "access token is required");

            final String adminUid = extractUidFromToken(adminToken);
            validateToken(adminUid, adminToken, TokenType.ACCESS);

            if (!userUid.equals(adminUid)) {
                final Set<Role> adminRoles = new HashSet<>();

                userRoleRepository.findRolesByUserId(adminUid)
                        .forEach(assoc -> {
                            final Role role = roleRepository.findByRoleId(assoc.getRoleId());
                            if (role != null) {
                                adminRoles.add(role);
                            }
                        });

                final boolean isAdmin = adminRoles.stream()
                        .anyMatch(role -> role.getRoleName().equals("Admin"));

                require(isAdmin, 403, "unauthorized to revoke token");
            }

            redisCacheManager.getCache("auth-token")
                    .evictIfPresent(userUid);

            logger.info("admin {} revoked token for user {}", adminUid, userUid);
        }
    }

    @PostMapping(value = "/api/oauth/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(
            final @RequestHeader Map<String, String> headers,
            final HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            final String token = extractTokenFromHeaders(headers);
            require(Strings.isNotBlank(token), 401, "bearer token is required");

            final String userId = extractUidFromToken(token);
            require(Strings.isNotBlank(userId), 400, "user id is required");

            validateToken(userId, token, TokenType.ACCESS);

            redisCacheManager.getCache("auth-token")
                    .evictIfPresent(userId);

            logger.info("user {} invalidated their own token", userId);
        }
    }

    private String generateToken(@NonNull String userId, @NonNull TokenType tokenType) {
        try {
            final Map<String, Object> headerMap = Map.of(
                    "alg", "HS256",
                    "typ", tokenType == TokenType.ACCESS ? "JWT" : "Refresh"
            );

            final Date now = new Date();

            final Date expires = Date.from(now
                    .toInstant()
                    .atOffset(ZoneOffset.UTC)
                    .toLocalDateTime()
                    .plusMinutes(tokenLifespan)
                    .toInstant(ZoneOffset.UTC));

            return JWT.create()
                    .withHeader(headerMap)
                    .withIssuedAt(now)
                    .withExpiresAt(expires)
                    .withSubject(userId)
                    .withIssuer(tokenIssuer)
                    .withClaim("perms", "{}")
                    .sign(algorithm);

        } catch (Exception e) {
            throw new Http500Exception(e);
        }
    }

    public void validateToken(@NonNull String userId, @NonNull String token, @NonNull TokenType tokenType) {
        try {
            require(!userId.isBlank(), 401, "invalid userId");
            require(!token.isBlank(), 401, "invalid token for " + userId);

            JWT.require(algorithm)
                    .withSubject(userId)
                    .withIssuer(tokenIssuer)
                    .build()
                    .verify(token);

            final Cache cache = redisCacheManager.getCache("auth-token");
            final JsonNode cachedValues = cache.get(userId, JsonNode.class);

            if (Strings.isBlank(cachedValues.at("/refresh_token").asText(""))) {
                throw new Http401Exception("token has been invalidated for " + userId);
            }

            final String incomingHash = generateHash(token);
            final String cachedHash = tokenType == TokenType.ACCESS
                    ? cachedValues.at("/access_token").asText("")
                    : cachedValues.at("/refresh_token").asText("");

            if (!cachedHash.equals(incomingHash)) {
                logger.warn("token mismatch for {}", userId);
                throw new Http401Exception("token mismatch for " + userId);
            }

        } catch (AlgorithmMismatchException e) {
            logger.warn("token has an algorithm mismatch for {}", userId);
            throw new Http401Exception("token has an algorithm mismatch for " + userId, e);

        } catch (SignatureVerificationException e) {
            logger.warn("token has an invalid signature for {}", userId);
            throw new Http401Exception("token has an invalid signature for " + userId, e);

        } catch (TokenExpiredException e) {
            logger.warn("token has expired for {}", userId);
            throw new Http401Exception("token has expired for " + userId, e);

        } catch (MissingClaimException e) {
            logger.warn("token is missing required claims for {}", userId);
            throw new Http401Exception("token is missing required claims for " + userId, e);

        } catch (IncorrectClaimException e) {
            logger.warn("token has invalid claim values for {}", userId);
            throw new Http401Exception("token has invalid claim values for " + userId, e);

        } catch (Exception e) {
            throw new Http401Exception("token is invalid for" + userId, e);
        }
    }
}