package com.evatech.bidplatform.common.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@Profile("!local")
public class KeycloakConfig {

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
