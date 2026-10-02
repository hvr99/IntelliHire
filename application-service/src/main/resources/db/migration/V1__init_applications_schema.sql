CREATE SCHEMA IF NOT EXISTS applications;

CREATE TABLE applications.applications (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    candidate_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_applications_job_candidate UNIQUE (job_id, candidate_id),
    CONSTRAINT ck_applications_status CHECK (status IN ('APPLIED','SHORTLISTED','INTERVIEW','OFFERED','REJECTED','WITHDRAWN'))
);

CREATE INDEX ix_applications_candidate_id ON applications.applications(candidate_id);
CREATE INDEX ix_applications_job_id ON applications.applications(job_id);

CREATE TABLE applications.status_history (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications.applications(id),
    old_status VARCHAR(32),
    new_status VARCHAR(32) NOT NULL,
    changed_at TIMESTAMP NOT NULL
);

CREATE INDEX ix_status_history_application_id ON applications.status_history(application_id);
