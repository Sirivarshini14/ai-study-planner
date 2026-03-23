-- Run this manually or via Flyway in production.
-- Spring JPA ddl-auto is set to 'validate' — it won't create tables.

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,
    mobile          VARCHAR(15)   NOT NULL UNIQUE,
    password_hash   VARCHAR(255)  NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_mobile ON users(mobile);

CREATE TABLE IF NOT EXISTS study_sessions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject         VARCHAR(100)  NOT NULL,
    topic           VARCHAR(255)  NOT NULL,
    start_time      TIMESTAMP     NOT NULL,
    end_time        TIMESTAMP,
    is_notified     BOOLEAN       NOT NULL DEFAULT FALSE,
    status          VARCHAR(20)   NOT NULL DEFAULT 'UPCOMING',
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON study_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_start_time ON study_sessions(start_time);

CREATE TABLE IF NOT EXISTS pomodoro_settings (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    focus_minutes   INT           NOT NULL DEFAULT 25,
    break_minutes   INT           NOT NULL DEFAULT 5,
    long_break_min  INT           NOT NULL DEFAULT 15,
    sessions_before_long_break INT NOT NULL DEFAULT 4,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id      BIGINT        REFERENCES study_sessions(id) ON DELETE SET NULL,
    role            VARCHAR(10)   NOT NULL,
    content         TEXT          NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_user_session ON chat_messages(user_id, session_id, created_at);

CREATE TABLE IF NOT EXISTS notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_id      BIGINT        NOT NULL REFERENCES study_sessions(id) ON DELETE CASCADE,
    message         VARCHAR(500)  NOT NULL,
    is_read         BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id, is_read, created_at);
