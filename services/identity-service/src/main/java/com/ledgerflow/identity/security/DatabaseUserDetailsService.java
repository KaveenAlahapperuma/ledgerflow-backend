package com.ledgerflow.identity.security;

import com.ledgerflow.identity.user.Role;
import com.ledgerflow.identity.user.UserAccount;
import com.ledgerflow.identity.user.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tells Spring Security how to find a user by the name typed on the login form.
 * Spring then checks the password and the "enabled" flag itself.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserAccountRepository users;

    public DatabaseUserDetailsService(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount account = users.findByEmailWithRoles(email)
                // Never say whether the email exists: that would let attackers list users.
                .orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));

        return User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .roles(account.getRoles().stream().map(Role::getName).toArray(String[]::new))
                .disabled(!account.isActive())   // DISABLED users are rejected by Spring
                .build();
    }
}
