package com.ledgerflow.identity.api;

import com.ledgerflow.identity.user.Role;
import com.ledgerflow.identity.user.UserAccount;
import com.ledgerflow.identity.user.UserAccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * "Who am I?" Returns the logged-in user's profile.
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
        UserAccount account = users.findByEmailWithRoles(authentication.getName()).orElseThrow();
        return MeResponse.from(account);
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
