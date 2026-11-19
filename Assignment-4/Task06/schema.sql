-- Campus Event Registration System Database Schema
-- Table: event_registrations

CREATE TABLE IF NOT EXISTS event_registrations (
    reg_id VARCHAR(32) PRIMARY KEY,
    student_id VARCHAR(32) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL,
    event_name VARCHAR(100) NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_student_event UNIQUE (student_id, event_name)
);

-- Index for high-performance event filtering
CREATE INDEX IF NOT EXISTS idx_event_name ON event_registrations(event_name);
