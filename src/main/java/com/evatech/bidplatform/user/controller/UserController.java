package com.evatech.bidplatform.user.controller;


import com.evatech.bidplatform.user.dto.UserLookupType;
import com.evatech.bidplatform.user.dto.request.LoginRequest;
import com.evatech.bidplatform.user.dto.request.RegisterRequest;
import com.evatech.bidplatform.user.dto.request.UpdateUserRequest;
import com.evatech.bidplatform.user.dto.response.UserResponse;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.exception.CustomException;
import com.evatech.bidplatform.user.mapper.UserMapper;
import com.evatech.bidplatform.user.repository.UserRepository;
import com.evatech.bidplatform.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepo;
    private final UserMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
            User user = null;
            try {
                user = userService.fetchUser(UserLookupType.EMAIL, request.getEmail());
            } catch (CustomException ex) {
                if(!ex.getMessage().equalsIgnoreCase("User not found")) {
                    throw ex;
                }
            }
            if(user != null) {
                throw new CustomException("Email already registered", 409);
            }

            User response = userService.register(request);
            UserResponse userResponse = mapper.toResponse(response);
            return ResponseEntity.ok().
                    header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(userResponse);
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(Authentication authentication,
                                    @RequestBody UpdateUserRequest updateUserRequest) {

        Jwt jwt = (Jwt) authentication.getPrincipal();

        String keycloakUserId = jwt.getSubject(); // UUID

        User user = userRepo.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new CustomException("User not found"));

        if(!user.isEnabled()) {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("User is not enabled yet, Please verify email");
        }
        User response = userService.updateDetails(user, updateUserRequest);
        UserResponse userResponse = mapper.toResponse(response);
        return ResponseEntity.ok().
                header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(userResponse);
    }



    @PostMapping("/resend/token")
    public ResponseEntity<?> resendToken(
            @RequestBody LoginRequest req
    ) {
        User user = userService.fetchUser(UserLookupType.EMAIL, req.getEmail());

        userService.sendVerificationToken(user);
        return ResponseEntity.ok(
                "Token Resent Successfully"
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();

        String keycloakUserId = jwt.getSubject(); // UUID

        User response = userRepo.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new CustomException("User not found"));

        if(!response.isEnabled()) {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("User is not enabled yet, Please verify email");
        }
        UserResponse userResponse = mapper.toResponse(response);
        return ResponseEntity.ok().
                header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(userResponse);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/fetch")
    public ResponseEntity<UserResponse> fetchUser(@RequestParam("phoneNumber") String phoneNumber,
                                                  @RequestParam("email") String email,
                                                  @RequestParam("username") String username) {
        if (phoneNumber == null && email == null && username == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one search parameter is required"
            );
        }
        User user;
       if (phoneNumber != null) {
            user = userService.fetchUser(UserLookupType.PHONE,phoneNumber);
        } else if (email != null) {
            user = userService.fetchUser(UserLookupType.EMAIL,email);
        } else  {
            user = userService.fetchUser(UserLookupType.USERNAME,username);
        }
        if(user != null) {
            UserResponse userResponse = mapper.toResponse(user);
            return ResponseEntity.ok().
                    header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(userResponse);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam("phoneNumber") String phoneNumber,
                                        @RequestParam("email") String email,
                                        @RequestParam("username") String username) {
        if (phoneNumber == null && email == null && username == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one search parameter is required"
            );
        }
        User user;
        if (phoneNumber != null) {
            user = userService.fetchUser(UserLookupType.PHONE,phoneNumber);
        } else if (email != null) {
            user = userService.fetchUser(UserLookupType.EMAIL,email);
        } else  {
            user = userService.fetchUser(UserLookupType.USERNAME,username);
        }
        if(user != null) {
            userService.deleteUser(user.getKeycloakUserId());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("User deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }


}
