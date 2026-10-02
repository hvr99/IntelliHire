CREATE TABLE users.suggested_skill (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    source VARCHAR(32) NOT NULL,
    confidence NUMERIC(5,4) NOT NULL,
    status VARCHAR(32) NOT NULL,
    source_document VARCHAR(255),
    CONSTRAINT suggested_skill_source_chk CHECK (source IN ('RESUME', 'CERTIFICATE')),
    CONSTRAINT suggested_skill_status_chk CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))
);

CREATE INDEX suggested_skill_user_status_idx ON users.suggested_skill (user_id, status);
