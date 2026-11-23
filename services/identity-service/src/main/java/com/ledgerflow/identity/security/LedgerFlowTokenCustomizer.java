package com.ledgerflow.identity.security;

import com.ledgerflow.identity.user.Role;
import com.ledgerflow.identity.user.UserAccountRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

/**
 * Adds LedgerFlow's own claims to every USER access token, so other services know
 * who the user is, which company they belong to and what they may do,
 * without ever calling the Identity Service.
 *
 * The authorization server finds this bean automatically and calls it just before
 * signing each token.
 */
@Component
public class LedgerFlowTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final UserAccountRepository users;

    public LedgerFlowTokenCustomizer(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        // Only access tokens (not ID tokens or anything else).
        if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            return;
        }
        // Machine tokens (client credentials) have no user behind them: leave them alone.
        if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {
            return;
        }

        String email = context.getPrincipal().getName();   // what the user typed on the login form

        users.findByEmailWithRoles(email).ifPresent(user -> context.getClaims().claims(claims -> {
            // "sub" becomes the user's permanent ID. An email can change; the ID never does,
            // so other services can safely store it (e.g. expenses.employee_id).
            claims.put("sub", user.getId().toString());
            claims.put("email", user.getEmail());
            claims.put("name", user.getFullName());

            // Multi-tenancy: every service reads this to keep companies' data apart.
            claims.put("tenant_id", user.getTenantId().toString());
            if (user.getDepartmentId() != null) {
                claims.put("department_id", user.getDepartmentId().toString());
            }

            claims.put("roles", user.getRoles().stream()
                    .map(Role::getName)
                    .sorted()
                    .toList());
        }));
    }
}
