-- One database and one user per service.
-- Each user can only connect to its own database.

CREATE USER identity_user     WITH PASSWORD 'identity_pass';
CREATE USER tenant_user       WITH PASSWORD 'tenant_pass';
CREATE USER expense_user      WITH PASSWORD 'expense_pass';
CREATE USER workflow_user     WITH PASSWORD 'workflow_pass';
CREATE USER ai_user           WITH PASSWORD 'ai_pass';
CREATE USER payment_user      WITH PASSWORD 'payment_pass';
CREATE USER notification_user WITH PASSWORD 'notification_pass';
CREATE USER audit_user        WITH PASSWORD 'audit_pass';

CREATE DATABASE identity_db     OWNER identity_user;
CREATE DATABASE tenant_db       OWNER tenant_user;
CREATE DATABASE expense_db      OWNER expense_user;
CREATE DATABASE workflow_db     OWNER workflow_user;
CREATE DATABASE ai_db           OWNER ai_user;
CREATE DATABASE payment_db      OWNER payment_user;
CREATE DATABASE notification_db OWNER notification_user;
CREATE DATABASE audit_db        OWNER audit_user;

-- By default every user can connect to every database. Remove that.
REVOKE CONNECT ON DATABASE identity_db     FROM PUBLIC;
REVOKE CONNECT ON DATABASE tenant_db       FROM PUBLIC;
REVOKE CONNECT ON DATABASE expense_db      FROM PUBLIC;
REVOKE CONNECT ON DATABASE workflow_db     FROM PUBLIC;
REVOKE CONNECT ON DATABASE ai_db           FROM PUBLIC;
REVOKE CONNECT ON DATABASE payment_db      FROM PUBLIC;
REVOKE CONNECT ON DATABASE notification_db FROM PUBLIC;
REVOKE CONNECT ON DATABASE audit_db        FROM PUBLIC;