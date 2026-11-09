-- Roles are fixed reference data, so they live in a real migration.
CREATE TABLE roles (
    id   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID         NOT NULL,          -- refers to tenant_db (no FK across databases)
    department_id UUID,                           -- refers to tenant_db (no FK across databases)
    email         VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,          -- BCrypt hash, never the password
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version       BIGINT       NOT NULL DEFAULT 0 -- optimistic locking
);

-- Email is the login name, unique across all tenants, case-insensitive.
CREATE UNIQUE INDEX ux_users_email ON users (lower(email));
CREATE INDEX ix_users_tenant ON users (tenant_id);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id),
    PRIMARY KEY (user_id, role_id)
);

INSERT INTO roles (name) VALUES
    ('EMPLOYEE'),
    ('MANAGER'),
    ('FINANCE'),
    ('TENANT_ADMIN');
