package com.evatech.bidplatform.common.config.local;

import com.evatech.bidplatform.MockAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class SecurityConfigLocal {

    private final MockAuthenticationFilter mockAuthenticationFilter;

    @Bean
    public SecurityFilterChain localSecurityFilterChain(
            HttpSecurity http) {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .addFilterBefore(
                        mockAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll());

        return http.build();
    }
}