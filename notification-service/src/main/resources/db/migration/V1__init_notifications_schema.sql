CREATE SCHEMA IF NOT EXISTS notifications;

CREATE TABLE notifications.notification (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    type VARCHAR(64) NOT NULL CHECK (type IN ('RECOMMENDED_JOB', 'APPLICATION_STATUS_CHANGED')),
    title VARCHAR(255) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    ref_id UUID,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_notification_user_created ON notifications.notification (user_id, created_at DESC);
CREATE UNIQUE INDEX uq_recommended_job_once ON notifications.notification (user_id, ref_id) WHERE type = 'RECOMMENDED_JOB' AND ref_id IS NOT NULL;
