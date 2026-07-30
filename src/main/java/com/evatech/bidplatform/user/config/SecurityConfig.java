package com.evatech.bidplatform.user.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
public class SecurityConfig {

    @Value("${keycloak.realm}")
    private String keyCloakRealm;

    @Value("${keycloak.url}")
    private String keyCloakUrl;

    @Value("${keycloak.username}")
    private String keyCloakUserName;

    @Value("${keycloak.password}")
    private String keyCloakPassword;

    @Value("${keycloak.clientid}")
    private String keyCloakClientId;

    @Value("${keycloak.clientsecret}")
    private String keyCloakClientSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        http
                .csrf(csrf -> csrf.disable())
                . cors(cors -> {})   // ✅ IMPORTANT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/user/register",
                                "/user/resend/token",
                                "/user/email/verify",
                                "/user/update/verify",
                                "/user/password/send-otp",
                                "/user/password/reset",
                                "/user/call-status",
                                "/user/analyze-audio",
                                "/user/voice",
                                "/user/recording-complete",
                                "/auth/token",
                                "/webhook/github",
                                "/reviews",
                                "/reviews/report"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    Keycloak keycloak() {
        log.info("keyCloakUrl {}, keyCloakRealm {}, keyCloakUserName {}, keyCloakPassword {}, keyCloakClientId {}, keyCloakClientSecret {}"
                ,keyCloakUrl, keyCloakRealm, keyCloakUserName, keyCloakPassword, keyCloakClientId, keyCloakClientSecret);
        return KeycloakBuilder.builder()
                .serverUrl(keyCloakUrl)
                .realm(keyCloakRealm)
                .username(keyCloakUserName)
                .password(keyCloakPassword)
                .clientId(keyCloakClientId)
                .clientSecret(keyCloakClientSecret)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
