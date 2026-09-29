# Database Initialization Guide

This document contains the exact Windows commands required to initialize the local PostgreSQL 16 database for the microservices.

## Prerequisites
- PostgreSQL 16 installed locally.
- `psql` available in your Command Prompt / PowerShell PATH.
- The `postgres` admin password.

---

## 1. Create the Databases

Open your PowerShell or Command Prompt and connect to the default `postgres` database to create the application and test databases:

```powershell
# Log into PostgreSQL as the admin user
psql -U postgres -c "CREATE DATABASE jobrec;"
psql -U postgres -c "CREATE DATABASE jobrec_test;"
```
*(If prompted, enter your `postgres` user password).*

---

## 2. Execute `init.sql`

The initialization script sets up schemas and roles. Passwords for the service roles are passed via `-v` variables to avoid hardcoding secrets.

Run this command in the `db/` directory, replacing `<password>` with your chosen local passwords:

```powershell
# For the main database
psql -U postgres -d jobrec `
  -v auth_pwd='<password>' `
  -v user_pwd='<password>' `
  -v job_pwd='<password>' `
  -v application_pwd='<password>' `
  -v recommendation_pwd='<password>' `
  -v notification_pwd='<password>' `
  -f init.sql

# For the test database (repeat the process)
psql -U postgres -d jobrec_test `
  -v auth_pwd='<password>' `
  -v user_pwd='<password>' `
  -v job_pwd='<password>' `
  -v application_pwd='<password>' `
  -v recommendation_pwd='<password>' `
  -v notification_pwd='<password>' `
  -f init.sql
```

The script is idempotent. You can execute it multiple times safely.

---

## 3. Verifying Schema Permissions

To test that schemas are correctly isolated (e.g., `job_svc` cannot access the `auth` schema), you can log in as a specific service user and attempt to create a table in another schema.

**Step A:** Log in as the `job_svc` user:
```powershell
psql -U job_svc -d jobrec
```
*(Enter the password you provided for `job_pwd`).*

**Step B:** Try to create a table in the `auth` schema (this should FAIL):
```sql
CREATE TABLE auth.test_table (id INT);
-- Expected output: ERROR: permission denied for schema auth
```

**Step C:** Create a table in the `jobs` schema (this should SUCCEED):
```sql
CREATE TABLE jobs.test_table (id INT);
-- Expected output: CREATE TABLE
```

**Step D:** Clean up the test table and exit:
```sql
DROP TABLE jobs.test_table;
\q
```
