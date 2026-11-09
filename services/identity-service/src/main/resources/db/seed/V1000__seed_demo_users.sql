-- DEV ONLY: demo users from the LedgerFlow story.
-- Loaded only in the "dev" profile (see application-dev.yml).
-- Fixed UUIDs so other services can seed matching tenants and departments.
--
-- Tenant  Acme Lanka     11111111-1111-1111-1111-111111111111
--   Dept  Engineering    a1111111-0000-0000-0000-000000000001
--   Dept  Finance        a1111111-0000-0000-0000-000000000002
-- Tenant  Globex         22222222-2222-2222-2222-222222222222
--   Dept  Sales          b2222222-0000-0000-0000-000000000001
--
-- All passwords: Password123!

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- for crypt() / gen_salt('bf'): BCrypt in SQL

INSERT INTO users (id, tenant_id, department_id, email, full_name, password_hash) VALUES
    ('c0000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111',
     'a1111111-0000-0000-0000-000000000002', 'dilani@acme.lk', 'Dilani Perera',
     crypt('Password123!', gen_salt('bf', 10))),
    ('c0000000-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111',
     'a1111111-0000-0000-0000-000000000001', 'kasun@acme.lk', 'Kasun Silva',
     crypt('Password123!', gen_salt('bf', 10))),
    ('c0000000-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111',
     'a1111111-0000-0000-0000-000000000001', 'nimal@acme.lk', 'Nimal Fernando',
     crypt('Password123!', gen_salt('bf', 10))),
    ('c0000000-0000-0000-0000-000000000004', '22222222-2222-2222-2222-222222222222',
     'b2222222-0000-0000-0000-000000000001', 'sara@globex.lk', 'Sara Jayasinghe',
     crypt('Password123!', gen_salt('bf', 10)));

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM (VALUES
        ('dilani@acme.lk', 'TENANT_ADMIN'),
        ('dilani@acme.lk', 'FINANCE'),
        ('kasun@acme.lk',  'MANAGER'),
        ('kasun@acme.lk',  'EMPLOYEE'),
        ('nimal@acme.lk',  'EMPLOYEE'),
        ('sara@globex.lk', 'EMPLOYEE')
     ) AS ur (email, role)
JOIN users u ON u.email = ur.email
JOIN roles r ON r.name  = ur.role;
