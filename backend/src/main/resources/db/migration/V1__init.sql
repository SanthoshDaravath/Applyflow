CREATE TABLE users (
    id binary(16) PRIMARY KEY,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp,
    email varchar(255) NOT NULL UNIQUE,
    password_hash varchar(255),
    full_name varchar(180) NOT NULL,
    provider varchar(40),
    provider_id varchar(255),
    role varchar(30) NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    email_verified boolean NOT NULL DEFAULT false,
    last_login_at timestamp
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

CREATE TABLE refresh_tokens (
    id binary(16) PRIMARY KEY,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp,
    token_hash varchar(255) NOT NULL UNIQUE,
    expires_at timestamp NOT NULL,
    revoked boolean NOT NULL DEFAULT false,
    user_id binary(16) NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

CREATE TABLE applications (
    id binary(16) PRIMARY KEY,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp,
    user_id binary(16) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company varchar(180) NOT NULL,
    role_name varchar(180) NOT NULL,
    salary numeric(12,2),
    location varchar(180),
    source_platform varchar(80) NOT NULL,
    application_date date NOT NULL,
    status varchar(40) NOT NULL,
    notes text,
    job_url text,
    resume_snapshot text
);

CREATE INDEX idx_applications_user_status ON applications (user_id, status);
CREATE INDEX idx_applications_company ON applications (company);
CREATE INDEX idx_applications_source ON applications (source_platform);

CREATE TABLE interviews (
    id binary(16) PRIMARY KEY,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp,
    application_id binary(16) NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    round_name varchar(120) NOT NULL,
    scheduled_at timestamp NOT NULL,
    location varchar(180),
    interview_type varchar(120),
    feedback text,
    notes text,
    status varchar(30) NOT NULL,
    reminder_sent boolean NOT NULL DEFAULT false
);

CREATE INDEX idx_interviews_application ON interviews (application_id);
CREATE INDEX idx_interviews_scheduled ON interviews (scheduled_at);

CREATE TABLE resumes (
    id binary(16) PRIMARY KEY,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp,
    user_id binary(16) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id binary(16) REFERENCES applications(id) ON DELETE SET NULL,
    title varchar(180) NOT NULL,
    file_name varchar(255),
    content_type varchar(120),
    storage_path varchar(500),
    file_data longblob,
    source varchar(30) NOT NULL,
    job_description text,
    resume_text text,
    ats_score integer,
    matched_keywords_json text,
    missing_keywords_json text,
    suggestions_json text
);

CREATE INDEX idx_resumes_user ON resumes (user_id);
CREATE INDEX idx_resumes_title ON resumes (title);

CREATE TABLE email_events (
    id binary(16) PRIMARY KEY,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp,
    user_id binary(16) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id binary(16) REFERENCES applications(id) ON DELETE SET NULL,
    message_id varchar(255) NOT NULL UNIQUE,
    thread_id varchar(255),
    from_address varchar(255),
    subject varchar(255) NOT NULL,
    body text NOT NULL,
    body_snippet text,
    classification varchar(40) NOT NULL,
    confidence double NOT NULL,
    metadata_json text,
    received_at timestamp NOT NULL,
    processed_at timestamp
);

CREATE INDEX idx_email_events_user ON email_events (user_id);
CREATE INDEX idx_email_events_classification ON email_events (classification);
CREATE INDEX idx_email_events_message_id ON email_events (message_id);

CREATE TABLE ai_insights (
    id binary(16) PRIMARY KEY,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp,
    user_id binary(16) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id binary(16) REFERENCES applications(id) ON DELETE SET NULL,
    insight_type varchar(60) NOT NULL,
    title varchar(255) NOT NULL,
    summary text,
    recommendations_json text,
    confidence double NOT NULL,
    generated_at timestamp NOT NULL
);

CREATE INDEX idx_ai_insights_user ON ai_insights (user_id);
CREATE INDEX idx_ai_insights_type ON ai_insights (insight_type);

CREATE TABLE notifications (
    id binary(16) PRIMARY KEY,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp,
    user_id binary(16) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id binary(16) REFERENCES applications(id) ON DELETE SET NULL,
    interview_id binary(16) REFERENCES interviews(id) ON DELETE SET NULL,
    channel varchar(30) NOT NULL,
    title varchar(255) NOT NULL,
    message text,
    status varchar(30) NOT NULL,
    scheduled_at timestamp,
    sent_at timestamp,
    related_entity_type varchar(80),
    related_entity_id varchar(80)
);

CREATE INDEX idx_notifications_user_status ON notifications (user_id, status);
CREATE INDEX idx_notifications_scheduled ON notifications (scheduled_at);
