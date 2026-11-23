package com.ledgerflow.identity.api;

import com.ledgerflow.identity.user.Role;
import com.ledgerflow.identity.user.UserAccount;
import com.ledgerflow.identity.user.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * "Who am I?" Returns the logged-in user's profile.
 * Works with both a browser login (session) and a Bearer JWT (e.g. through the gateway).
 * Never returns the entity itself (it holds the password hash): always a DTO.
 */
@RestController
@RequestMapping("/api")
public class MeController {

    private final UserAccountRepository users;

    public MeController(UserAccountRepository users) {
        this.users = users;
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        Optional<UserAccount> account;

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            // Machine tokens (client credentials) have no user, so no tenant_id claim.
            if (!jwtAuth.getToken().hasClaim("tenant_id")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a user token");
            }
            // In our tokens, "sub" is the user's ID.
            account = users.findByIdWithRoles(UUID.fromString(jwtAuth.getName()));
        } else {
            // Browser session: the name is the email typed on the login form.
            account = users.findByEmailWithRoles(authentication.getName());
        }

        return MeResponse.from(account.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
    }

    public record MeResponse(
            UUID id,
            UUID tenantId,
            UUID departmentId,
            String email,
            String fullName,
            List<String> roles) {

        static MeResponse from(UserAccount account) {
            return new MeResponse(
                    account.getId(),
                    account.getTenantId(),
                    account.getDepartmentId(),
                    account.getEmail(),
                    account.getFullName(),
                    account.getRoles().stream().map(Role::getName).sorted().toList());
        }
    }
}
