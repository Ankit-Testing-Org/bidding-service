package com.evatech.bidplatform.contract.controller;

import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.exception.CustomException;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractController {

    protected User authenticateAndFetchUser(UserRepository userRepo,
                                          Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();

        String keycloakUserId = jwt.getSubject(); // UUID
        User user = userRepo.findByKeycloakUserId(keycloakUserId).orElseThrow(() -> new CustomException("User not found"));
        return user;
    }

    protected List<String> fetchRolesForUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");
        log.info("Roles {"+roles.toString()+"}");
        return roles;
    }
}
