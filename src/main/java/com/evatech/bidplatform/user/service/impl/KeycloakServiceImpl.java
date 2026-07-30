package com.evatech.bidplatform.user.service.impl;

import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.exception.CustomException;
import com.evatech.bidplatform.user.service.KeycloakService;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    @Value("${keycloak.url}")
    private String baseUrl;

    private final Keycloak keycloak;

    @Override
    public String createUser(String username, String email, String password,
                             String firstName, String lastName) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        cred.setTemporary(false);
        user.setCredentials(List.of(cred));
        Response response = keycloak.realm("master").users().create(user);

        if (response.getStatus() != 201) {
            String errorBody = response.readEntity(String.class);
            System.out.println("Keycloak error: " + errorBody);
            throw new CustomException("Failed to create Keycloak user");
        }

        String location = response.getHeaderString("Location");
        return location.substring(location.lastIndexOf("/") + 1);
    }

    @Override
    public void assignRealmRoles(String userId, List<String> roles) {

        for (RoleRepresentation master : keycloak.realm("master")
                .roles().list()) {
            log.info("master name "+master.getName());
        }

        // Get the realm roles
        RoleRepresentation roleToAssign = keycloak.realm("master")
                .roles()
                .get("admin") // role name
                .toRepresentation();
        log.info(" roleToAssign "+roleToAssign);
        if (roleToAssign == null) {
            throw new CustomException("No matching realm roles found for user");
        }

        // Assign roles to user
        keycloak.realm("master")
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .add(List.of(roleToAssign));
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        if (user.getKeycloakUserId() == null) {
            throw new IllegalStateException("Keycloak user ID missing");
        }
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false);
        try {
           keycloak
                    .realm("master")
                    .users()
                    .get(user.getKeycloakUserId())
                    .resetPassword(credential);

            log.info("Password updated in Keycloak for user {}", user.getUserName());

        } catch (BadRequestException e) {
            throw new CustomException(
                    "Password rejected by Keycloak policy"+ e.getMessage());
        }
    }

    @Override
    public void deleteUser(User user) {
        if (user.getKeycloakUserId() == null) {
            throw new IllegalStateException("Keycloak user ID missing");
        }
        try {
            keycloak
                    .realm("master")
                    .users()
                    .get(user.getKeycloakUserId())
                    .remove();

            log.info("User deleted in Keycloak with username {}", user.getUserName());

        } catch (BadRequestException e) {
            throw new CustomException(
                    "Failed to delete user by Keycloak policy"+ e.getMessage());
        }
    }

    @Override
    public String getAccessToken() {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getToken().getTokenValue();
    }

}