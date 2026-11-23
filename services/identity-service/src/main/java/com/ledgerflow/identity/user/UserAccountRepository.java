package com.ledgerflow.identity.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    /**
     * Loads a user and their roles in ONE query (join fetch avoids the N+1 problem
     * and LazyInitializationException).
     * Uses lower(email) so Postgres can use the ux_users_email index on lower(email).
     */
    @Query("""
            select u from UserAccount u
            left join fetch u.roles
            where lower(u.email) = lower(:email)
            """)
    Optional<UserAccount> findByEmailWithRoles(@Param("email") String email);

    /**
     * Same, but by ID: used when the caller is identified by a JWT, whose "sub" is the user ID.
     */
    @Query("""
            select u from UserAccount u
            left join fetch u.roles
            where u.id = :id
            """)
    Optional<UserAccount> findByIdWithRoles(@Param("id") UUID id);
}
