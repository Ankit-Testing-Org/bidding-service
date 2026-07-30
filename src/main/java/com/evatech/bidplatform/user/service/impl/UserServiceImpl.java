package com.evatech.bidplatform.user.service.impl;


import com.evatech.bidplatform.user.dto.UserLookupType;
import com.evatech.bidplatform.user.dto.request.RegisterRequest;
import com.evatech.bidplatform.user.dto.request.UpdateUserRequest;
import com.evatech.bidplatform.user.entity.EmailVerificationToken;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.exception.CustomException;
import com.evatech.bidplatform.user.repository.EmailVerificationTokenRepository;
import com.evatech.bidplatform.user.repository.UserRepository;
import com.evatech.bidplatform.user.service.KeycloakService;
import com.evatech.bidplatform.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final EmailVerificationTokenRepository tokenRepo;
    private final EmailServiceImpl emailService;
    private final KeycloakService keycloakService;

    @Override
    public void deleteUser(@NonNull String username) {
        User user = userRepo.findByUserName(username)
                .orElseThrow(() -> new CustomException("User not found", 400));

        keycloakService.deleteUser(user);

        userRepo.delete(user);
       log.info("User deleted for user id "+user.getKeycloakUserId());
    }

    @Override
    public User register(@NonNull RegisterRequest request) {

        //  Create user in Keycloak (AUTH SOURCE)
        String keycloakUserId = keycloakService.createUser(
                request.getUserName(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
        );

        //   Assigning role to user.
        keycloakService.assignRealmRoles(keycloakUserId, List.of("user"));

        //  Create local DB user (PROFILE SOURCE)
        User user = new User();
        user.setKeycloakUserId(keycloakUserId);
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddressLine1(request.getAddressLine1());
        user.setAddressLine2(request.getAddressLine2());
        user.setPostCode(request.getPostCode());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setCountry(request.getCountry());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setTermsAccepted(request.getTermsAccepted());
        // App-specific fields
        user.setBarcodeValue(UUID.randomUUID().toString());
        user.setEnabled(false); // enabled after email verification

        //  Save everything
        userRepo.save(user);

        //  Send email verification
        sendVerificationToken(user);

        return user;
    }

    @Override
    public void sendVerificationToken(@NonNull User user) {
        String token = UUID.randomUUID().toString();

        EmailVerificationToken ev = new EmailVerificationToken();
        ev.setToken(token);
        ev.setUser(user);
        ev.setExpiry(LocalDateTime.now().plusHours(24));

        tokenRepo.save(ev);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Override
    public User fetchUser(@NonNull UserLookupType type, @NonNull String value) {
         switch (type) {
             case PHONE -> {
                 return userRepo.findByPhoneNumber(value).orElseThrow(() ->
                         new CustomException("User not found", 400)
                 );
             }
             case EMAIL -> {
                 return userRepo.findByEmail(value).orElseThrow(() ->
                         new CustomException("User not found", 400)
                 );
             }
             case USERNAME -> {
                 return userRepo.findByUserName(value).orElseThrow(() ->
                        new CustomException("User not found", 400)
                );
             }
             case BARCODE -> {
                 return userRepo.findByBarcodeValue(value).orElseThrow(() ->
                        new CustomException("User not found", 400)
                );
             }
         }
         return null;
    }


    @Override
    @Transactional
    public User updateDetails(@NonNull User user, @NonNull UpdateUserRequest req) {

        updateIfChanged(user.getEmail(), req.getEmail(), user::setEmail);
        updateIfChanged(user.getPhoneNumber(), req.getPhoneNumber(), user::setPhoneNumber);
        updateIfChanged(user.getAddressLine1(), req.getAddressLine1(), user::setAddressLine1);
        updateIfChanged(user.getAddressLine2(), req.getAddressLine2(), user::setAddressLine2);
        updateIfChanged(user.getCity(), req.getCity(), user::setCity);
        updateIfChanged(user.getState(), req.getState(), user::setState);
        updateIfChanged(user.getCountry(), req.getCountry(), user::setCountry);
        updateIfChanged(user.getPostCode(), req.getPostCode(), user::setPostCode);

        return userRepo.save(user);
    }


    private void updateIfChanged(String oldValue, String newValue,
                                 @NonNull Consumer<String> setter) {
        if (!Objects.equals(
                normalize(oldValue),
                normalize(newValue)
        )) {
            setter.accept(newValue);
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

}

