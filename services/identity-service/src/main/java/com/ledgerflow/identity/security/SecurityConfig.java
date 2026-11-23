package com.ledgerflow.identity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Everything that is NOT an OAuth2 protocol endpoint: the login page, actuator, /api/me.
 * Runs second (@Order(2)); the authorization server chain gets first pick of requests.
 */
@Configuration
public class SecurityConfig {

    @Bean
    @Order(2)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                // The login page users see when the authorization server needs them to sign in.
                .formLogin(Customizer.withDefaults());
        // httpBasic removed: from now on, clients get tokens through OAuth2 instead.
        return http.build();
    }

    /**
     * Used for user passwords AND client secrets (the authorization server picks this bean up).
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
