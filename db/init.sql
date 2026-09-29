-- ==============================================================================
-- PostgreSQL Initialization Script for Job Recommendation Platform
-- ==============================================================================
-- This script creates the isolated schemas and roles for each microservice.
-- It is designed to be idempotent and safe to execute multiple times.
-- 
-- Usage (Requires passing passwords as variables):
-- psql -U postgres -d jobrec -v auth_pwd='<pwd>' -v user_pwd='<pwd>' -v job_pwd='<pwd>' -v application_pwd='<pwd>' -v recommendation_pwd='<pwd>' -v notification_pwd='<pwd>' -f init.sql
-- ==============================================================================

-- 1. Secure the public schema (prevent default access)
REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- 2. Create service roles (Idempotent block)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'auth_svc') THEN
        CREATE ROLE auth_svc LOGIN PASSWORD :'auth_pwd';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'user_svc') THEN
        CREATE ROLE user_svc LOGIN PASSWORD :'user_pwd';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'job_svc') THEN
        CREATE ROLE job_svc LOGIN PASSWORD :'job_pwd';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'application_svc') THEN
        CREATE ROLE application_svc LOGIN PASSWORD :'application_pwd';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'recommendation_svc') THEN
        CREATE ROLE recommendation_svc LOGIN PASSWORD :'recommendation_pwd';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'notification_svc') THEN
        CREATE ROLE notification_svc LOGIN PASSWORD :'notification_pwd';
    END IF;
END
$$;

-- 3. Create schemas for each service
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS jobs;
CREATE SCHEMA IF NOT EXISTS applications;
CREATE SCHEMA IF NOT EXISTS recommendation;
CREATE SCHEMA IF NOT EXISTS notification;

-- 4. Set ownership and explicitly revoke public access
-- Auth Service
ALTER SCHEMA auth OWNER TO auth_svc;
REVOKE ALL ON SCHEMA auth FROM PUBLIC;

-- User Service
ALTER SCHEMA users OWNER TO user_svc;
REVOKE ALL ON SCHEMA users FROM PUBLIC;

-- Job Service
ALTER SCHEMA jobs OWNER TO job_svc;
REVOKE ALL ON SCHEMA jobs FROM PUBLIC;

-- Application Service
ALTER SCHEMA applications OWNER TO application_svc;
REVOKE ALL ON SCHEMA applications FROM PUBLIC;

-- Recommendation Service
ALTER SCHEMA recommendation OWNER TO recommendation_svc;
REVOKE ALL ON SCHEMA recommendation FROM PUBLIC;

-- Notification Service
ALTER SCHEMA notification OWNER TO notification_svc;
REVOKE ALL ON SCHEMA notification FROM PUBLIC;

-- End of initialization script. Flyway will handle creating tables within these schemas.
