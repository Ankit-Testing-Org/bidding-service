package com.evatech.bidplatform.user.service.impl;

import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.service.KeycloakService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Profile("local")
public class MockKeycloakServiceImpl implements KeycloakService {

    @Override
    public String createUser(
            String username,
            String email,
            String password,
            String firstName,
            String lastName) {

        return UUID.randomUUID().toString();
    }

    @Override
    public void assignRealmRoles(
            String userId,
            List<String> roles) {

        // no-op
    }

    @Override
    public void updatePassword(User user, String newPassword) {

    }

    @Override
    public void deleteUser(User user) {

    }

    @Override
    public void requireTotpSetup(String keycloakUserId) {

    }

    @Override
    public void removeRealmRoles(String keycloakUserId, List<String> roles) {

    }

    @Override
    public void replaceRealmRoles(String keycloakUserId, List<String> roles) {

    }

    @Override
    public Set<String> getUserRoles(String keycloakUserId) {
        return Set.of("TEST");
    }
}
