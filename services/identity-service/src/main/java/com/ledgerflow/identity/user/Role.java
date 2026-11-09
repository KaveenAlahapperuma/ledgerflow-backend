package com.ledgerflow.identity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;   // EMPLOYEE, MANAGER, FINANCE, TENANT_ADMIN

    protected Role() {
        // for JPA
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
