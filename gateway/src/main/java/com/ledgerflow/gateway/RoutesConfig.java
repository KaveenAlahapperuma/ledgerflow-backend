package com.ledgerflow.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Which URL goes to which service. One route per service; more are added as services are built.
 *
 *   GET http://localhost:8080/api/identity/me  ->  GET http://localhost:9000/api/me
 *
 * The Authorization header is passed through unchanged, so the service can read the user too.
 */
@Configuration
public class RoutesConfig {

    @Bean
    RouteLocator routes(RouteLocatorBuilder builder,
                        @Value("${ledgerflow.services.identity}") String identityUri) {
        return builder.routes()
                .route("identity", r -> r
                        .path("/api/identity/**")
                        .filters(f -> f.rewritePath("/api/identity/(?<rest>.*)", "/api/${rest}"))
                        .uri(identityUri))
                .build();
    }
}
