package com.evatech.bidplatform.user.service;

import com.evatech.bidplatform.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public interface KeycloakService {

    String createUser(@NotBlank(message = "Username is required") String userName, @Email(message = "Invalid email") @NotBlank(message = "Email is required") String email, @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password, @NotBlank(message = "First name is required") String firstName, @NotBlank(message = "Last name is required") String lastName);

    void assignRealmRoles(String userId, List<String> roles);

    void updatePassword(User user, String newPassword);

    void deleteUser(User user);

    void requireTotpSetup(String keycloakUserId);

    void removeRealmRoles(
            String keycloakUserId,
            List<String> roles
    );

    void replaceRealmRoles(
            String keycloakUserId,
            List<String> roles
    );

    Set<String> getUserRoles(
            String keycloakUserId
    );
}
