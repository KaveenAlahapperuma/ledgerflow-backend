package com.ledgerflow.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * The gateway is a RESOURCE SERVER: it never logs anyone in, it only checks tokens.
 * Reactive (WebFlux) style, because Spring Cloud Gateway runs on Netty, not Tomcat.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                // No cookies or sessions here, only Bearer tokens, so CSRF protection isn't needed.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health").permitAll()
                        .anyExchange().authenticated())
                // Every other request must carry a valid JWT: right signature, not expired, right issuer.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
