package com.evatech.bidplatform.document.controller;

import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.exception.CustomException;
import com.evatech.bidplatform.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public abstract class AbstractController {

    protected User authenticateAndFetchUser(UserRepository userRepo,
                                          Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();

        String keycloakUserId = jwt.getSubject(); // UUID

        return userRepo.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new CustomException("User not found"));
    }
}
