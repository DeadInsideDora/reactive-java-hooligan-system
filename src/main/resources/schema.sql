CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    isu VARCHAR(7) UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    faculty VARCHAR(128),
    group_name VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS incidents (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    place VARCHAR(200) NOT NULL,
    type VARCHAR(32) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    offender_id UUID NOT NULL,
    created_by_id UUID NOT NULL,
    moderation_status VARCHAR(32) NOT NULL,
    punishment VARCHAR(32) NOT NULL DEFAULT 'NONE',
    department VARCHAR(32),
    CONSTRAINT fk_incident_offender FOREIGN KEY (offender_id) REFERENCES users(id),
    CONSTRAINT fk_incident_creator FOREIGN KEY (created_by_id) REFERENCES users(id)
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS isu VARCHAR(7);
CREATE UNIQUE INDEX IF NOT EXISTS uq_users_isu ON users (isu);
ALTER TABLE incidents ADD COLUMN IF NOT EXISTS department VARCHAR(32);
