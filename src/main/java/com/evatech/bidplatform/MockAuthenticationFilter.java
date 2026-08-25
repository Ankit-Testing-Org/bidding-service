package com.evatech.bidplatform;

import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Profile("local")
@RequiredArgsConstructor
public class MockAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String mockUser =
                request.getHeader("X-Mock-User");

        Jwt jwt = buildMockJwt(mockUser);

        Authentication auth =
                new JwtAuthenticationToken(jwt);

        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private Jwt buildMockJwt(String user) {

        return switch (user == null ? "admin" : user) {

            case "bidder" -> createJwt(
                    "bidder@test.com",
                    "bidder",
                    List.of("CONTRACT_OWNER, BID_MANAGER"));

            case "reviewer" -> createJwt(
                    "reviewer@test.com",
                    "reviewer",
                    List.of("REVIEWER, LEGAL_REVIEWER, FINANCE_REVIEWER," +
                            "COMMERCIAL_REVIEWER"));

            case "manager" -> createJwt(
                    "manager@test.com",
                    "manager",
                    List.of("MANAGER, MANAGEMENT_REVIEWER"));

            default -> createJwt(
                    "admin@test.com",
                    "admin",
                    List.of("ADMIN"));
        };
    }

    private Jwt createJwt(
            String email,
            String username,
            List<String> roles) {

        Jwt jwt = Jwt.withTokenValue("mock")
                .subject(UUID.randomUUID().toString())
                .claim("email", email)
                .claim("preferred_username", username)
                .claim("realm_access",
                        Map.of("roles", roles))
                .header("alg", "none")
                .build();
        createUserIfMissing(jwt);
        return jwt;
    }

    private void createUserIfMissing(Jwt jwt) {

        String userId = jwt.getSubject();

        Optional<User>  optionalUser = userRepository.findByKeycloakUserId(userId);
        if (optionalUser.isPresent()) {
            return;
        }
        User user = new User();
        user.setFirstName("test");
        user.setLastName("test");
        user.setKeycloakUserId(userId);
        user.setUserName(jwt.getClaimAsString("preferred_username"));
        user.setEmail(jwt.getClaimAsString("email"));
        user.setEnabled(true);

        userRepository.save(user);
    }
}