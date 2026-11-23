package com.ledgerflow.identity.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

/**
 * Turns the Identity Service into an OAuth2 Authorization Server.
 * Endpoints it adds: /oauth2/authorize, /oauth2/token, /oauth2/jwks, /oauth2/revoke,
 * /oauth2/introspect and /.well-known/oauth-authorization-server.
 */
@Configuration
public class AuthorizationServerConfig {

    /**
     * Chain 1: handles ONLY the OAuth2 protocol endpoints (securityMatcher limits it to them).
     */
    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .oauth2AuthorizationServer(authorizationServer ->
                        http.securityMatcher(authorizationServer.getEndpointsMatcher()))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                // A browser hitting /oauth2/authorize without being logged in is sent to /login.
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
        return http.build();
    }

    /**
     * The applications allowed to ask for tokens. In memory for now; moved to the
     * database (oauth2_registered_client) in a later session.
     */
    @Bean
    RegisteredClientRepository registeredClientRepository(
            PasswordEncoder passwordEncoder,
            @Value("${ledgerflow.auth.internal-client-secret}") String internalClientSecret) {

        // 1) The React app. A PUBLIC client: code running in a browser can't keep a secret,
        //    so it has none and must use PKCE instead.
        RegisteredClient web = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("ledgerflow-web")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                // Spring Authorization Server rejects "localhost" here; loopback must be 127.0.0.1.
                .redirectUri("http://127.0.0.1:5173/callback")       // React (Vite) dev server, Phase 7
                .redirectUri("https://oauth.pstmn.io/v1/callback")   // Postman, for testing now
                .scope("expense:read")
                .scope("expense:write")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)                // PKCE is mandatory
                        .requireAuthorizationConsent(false)   // our own app: no "allow access?" screen
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .build())
                .build();

        // 2) Our own services calling each other (e.g. Payment -> Tenant).
        //    A CONFIDENTIAL client: it runs on a server, so it can keep a secret.
        RegisteredClient internal = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("ledgerflow-internal")
                .clientSecret(passwordEncoder.encode(internalClientSecret)) // stored hashed, like passwords
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("budget:reserve")
                .scope("user:read")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(5))
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(web, internal);
    }

    /**
     * The RSA key pair that signs every token (RS256).
     * Private key: stays here and signs. Public key: published at /oauth2/jwks for other services.
     * Generated at startup for now, so tokens stop working after a restart.
     * Moved to the signing_keys table, with rotation, in a later session.
     */
    @Bean
    JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(UUID.randomUUID().toString())   // "kid": tells verifiers which key signed a token
                .build();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate RSA key pair", ex);
        }
    }

    /**
     * The issuer URL appears as "iss" in every token. Other services will only trust tokens
     * whose issuer matches exactly.
     */
    @Bean
    AuthorizationServerSettings authorizationServerSettings(@Value("${ledgerflow.auth.issuer}") String issuer) {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }
}
