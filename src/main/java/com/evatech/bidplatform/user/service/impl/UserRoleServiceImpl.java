package com.evatech.bidplatform.user.service.impl;

import com.evatech.bidplatform.user.dto.RoleType;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.exception.CustomException;
import com.evatech.bidplatform.user.repository.UserRepository;
import com.evatech.bidplatform.user.service.KeycloakService;
import com.evatech.bidplatform.user.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    @Override
    public void addRole(
            String email,
            RoleType role
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));
        keycloakService.assignRealmRoles(
                user.getKeycloakUserId(),
                List.of(role.name())
        );
    }

    @Override
    public void removeRole(
            String email,
            RoleType role
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));
        keycloakService.removeRealmRoles(
                user.getKeycloakUserId(),
                List.of(role.name())
        );
    }

    @Override
    public void replaceRoles(
            String email,
            Set<RoleType> roles
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));
        keycloakService.replaceRealmRoles(
                user.getKeycloakUserId(),
                roles.stream()
                        .map(Enum::name)
                        .toList()
        );
    }

    @Override
    public Set<String> getRoles(
            String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));
        return keycloakService.getUserRoles(
                user.getKeycloakUserId()
        );
    }
}
