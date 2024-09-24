package com.piercey.controllers;

import com.piercey.ExecutionTimer;
import com.piercey.exceptions.Http500Exception;
import com.piercey.models.Role;
import com.piercey.models.UserRole;
import com.piercey.repositories.RoleRepository;
import com.piercey.repositories.UserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
class RoleController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @RequestMapping(value = "/api/roles", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Role> getAllRoles(
            final @RequestParam(value = "companyId", required = false) String companyId,
            final @RequestParam(value = "userId", required = false) String userId,
            final @RequestParam(value = "roleName", required = false) String roleName,
            final HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {

            if (Strings.isNotBlank(companyId) && Strings.isNotBlank(userId)) {
                return roleRepository.findByCompanyId(companyId).stream()
                        .filter(role -> userRoleRepository.findRolesByUserId(userId).stream()
                                .map(UserRole::getRoleId)
                                .anyMatch(role.getId()::equals))
                        .toList();
            }

            if (Strings.isNotBlank(companyId) && Strings.isNotBlank(roleName)) {
                return List.of(roleRepository.findByNameAndCompanyId(roleName, companyId));
            }

            if (Strings.isNotBlank(companyId)) {
                return roleRepository.findByCompanyId(companyId);
            }

            return roleRepository.findAll();
        }
    }

    @RequestMapping(value = "/api/role/{roleId}", method = GET)
    @ResponseStatus(HttpStatus.OK)
    public Role getOneRole(
            final @PathVariable(value = "roleId", required = true) String roleId,
            final HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            return roleRepository.findByRoleId(roleId);
        }
    }

    @RequestMapping(value = "/api/role", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Role createRole(
            final @RequestBody Role role,
            final HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            require(role != null, 400, "requires a request body");

            sanitizeUserInput(role, role.getClass());

            return upsert(role);
        }
    }

    @RequestMapping(value = "/api/role/{roleId}", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public Role updateRole(
            final @PathVariable(value = "roleId") String roleId,
            final @RequestBody Role role, HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            require(Strings.isNotBlank(roleId), 400, "requires roleId");
            sanitizeUserInput(role, role.getClass());

            role.setId(sanitizeUserInput(roleId));

            return upsert(role);
        }
    }

    @RequestMapping(value = "/api/role/{roleId}", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteRole(
            final @PathVariable(value = "roleId") String roleId,
            final HttpServletRequest request) {

        try (ExecutionTimer t = new ExecutionTimer(logger, extractConnInfo(request), extractTraceInfo(request))) {
            require(Strings.isNotBlank(roleId), 400, "requires roleId");

            final String id = sanitizeUserInput(roleId);

            userRoleRepository.deleteRoleByRoleId(id);
            roleRepository.deleteById(id);
        }
    }

    private Role upsert(final @NonNull Role o) {
        return roleRepository.findById(o.getId())
                .map(company -> {
                    Arrays.stream(Role.class.getDeclaredFields())
                            .forEach(field -> {
                                try {
                                    field.setAccessible(true);
                                    if (field.get(o) != null) {
                                        field.set(company, field.get(o));
                                    }
                                } catch (Exception e) {
                                    throw new Http500Exception(e);
                                }
                            });
                    return roleRepository.save(company);
                })
                .orElseGet(() -> roleRepository.save(o));
    }
}