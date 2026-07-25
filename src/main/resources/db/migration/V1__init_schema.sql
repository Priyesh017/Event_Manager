-- V1__init_schema.sql
-- EventHub Database Schema — Initial Migration

-- ─────────────────────────────────────────────────────
-- USERS TABLE
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    role        VARCHAR(20)   NOT NULL DEFAULT 'ROLE_USER',
    enabled     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ─────────────────────────────────────────────────────
-- SPEAKERS TABLE
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS speakers (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    bio         TEXT,
    photo_url   VARCHAR(500)
);

-- ─────────────────────────────────────────────────────
-- EVENTS TABLE
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS events (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(200)  NOT NULL,
    description         TEXT,
    category            VARCHAR(50)   NOT NULL,
    event_date          TIMESTAMP     NOT NULL,
    venue               VARCHAR(200)  NOT NULL,
    location            VARCHAR(200)  NOT NULL,
    capacity            INTEGER       NOT NULL DEFAULT 100,
    registration_count  INTEGER       NOT NULL DEFAULT 0,
    image_url           VARCHAR(500),
    created_by          BIGINT        REFERENCES users(id) ON DELETE SET NULL,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_events_category    ON events(category);
CREATE INDEX IF NOT EXISTS idx_events_event_date  ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_location    ON events(location);

-- ─────────────────────────────────────────────────────
-- EVENT_SPEAKERS JOIN TABLE (Many-to-Many)
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS event_speakers (
    event_id    BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    speaker_id  BIGINT NOT NULL REFERENCES speakers(id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, speaker_id)
);

-- ─────────────────────────────────────────────────────
-- REGISTRATIONS TABLE
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS registrations (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id        BIGINT    NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    registered_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    attended        BOOLEAN   NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, event_id)
);

CREATE INDEX IF NOT EXISTS idx_registrations_user_id  ON registrations(user_id);
CREATE INDEX IF NOT EXISTS idx_registrations_event_id ON registrations(event_id);
