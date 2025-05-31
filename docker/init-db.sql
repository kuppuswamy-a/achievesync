-- Create databases for each microservice
CREATE DATABASE IF NOT EXISTS achievesync_user_db;
CREATE DATABASE IF NOT EXISTS achievesync_goal_db;

-- Use User Database
USE achievesync_user_db;

-- User Service Tables
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);

CREATE TABLE IF NOT EXISTS consistency_points (
    user_id VARCHAR(255) PRIMARY KEY,
    total_points INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Axon Framework Event Store Tables for User Service
CREATE TABLE IF NOT EXISTS domain_event_entry (
    global_index BIGINT NOT NULL AUTO_INCREMENT,
    event_identifier VARCHAR(255) NOT NULL UNIQUE,
    meta_data MEDIUMBLOB,
    payload MEDIUMBLOB NOT NULL,
    payload_revision VARCHAR(255),
    payload_type VARCHAR(255) NOT NULL,
    time_stamp VARCHAR(255) NOT NULL,
    aggregate_identifier VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    type VARCHAR(255),
    PRIMARY KEY (global_index),
    UNIQUE KEY UK_8s4f994j1crhqc5e0n7q3s1c7 (aggregate_identifier, sequence_number),
    KEY IDX_8s4f994j1crhqc5e0n7q3s1c7 (time_stamp)
);

CREATE TABLE IF NOT EXISTS snapshot_event_entry (
    aggregate_identifier VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    event_identifier VARCHAR(255) NOT NULL UNIQUE,
    meta_data MEDIUMBLOB,
    payload MEDIUMBLOB NOT NULL,
    payload_revision VARCHAR(255),
    payload_type VARCHAR(255) NOT NULL,
    time_stamp VARCHAR(255) NOT NULL,
    PRIMARY KEY (aggregate_identifier, sequence_number, type)
);

-- Use Goal Database
USE achievesync_goal_db;

-- Goal Service Tables
CREATE TABLE IF NOT EXISTS goals (
    goal_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    target_date DATE,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'PENDING',
    progress_percentage DOUBLE DEFAULT 0.0,
    category VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_category (category)
);

CREATE TABLE IF NOT EXISTS goal_tags (
    goal_id VARCHAR(255) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (goal_id, tag),
    FOREIGN KEY (goal_id) REFERENCES goals(goal_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS goal_progress (
    progress_id VARCHAR(255) PRIMARY KEY,
    goal_id VARCHAR(255) NOT NULL,
    progress_percentage DOUBLE NOT NULL,
    notes TEXT,
    update_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goal_id) REFERENCES goals(goal_id) ON DELETE CASCADE,
    INDEX idx_goal_id (goal_id),
    INDEX idx_timestamp (update_timestamp)
);

CREATE TABLE IF NOT EXISTS goal_streaks (
    goal_id VARCHAR(255) PRIMARY KEY,
    current_streak_days INT DEFAULT 0,
    longest_streak_days INT DEFAULT 0,
    last_streak_update DATE,
    is_streak_active BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (goal_id) REFERENCES goals(goal_id) ON DELETE CASCADE
);

-- Axon Framework Event Store Tables for Goal Service
CREATE TABLE IF NOT EXISTS domain_event_entry (
    global_index BIGINT NOT NULL AUTO_INCREMENT,
    event_identifier VARCHAR(255) NOT NULL UNIQUE,
    meta_data MEDIUMBLOB,
    payload MEDIUMBLOB NOT NULL,
    payload_revision VARCHAR(255),
    payload_type VARCHAR(255) NOT NULL,
    time_stamp VARCHAR(255) NOT NULL,
    aggregate_identifier VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    type VARCHAR(255),
    PRIMARY KEY (global_index),
    UNIQUE KEY UK_8s4f994j1crhqc5e0n7q3s1c7 (aggregate_identifier, sequence_number),
    KEY IDX_8s4f994j1crhqc5e0n7q3s1c7 (time_stamp)
);

CREATE TABLE IF NOT EXISTS snapshot_event_entry (
    aggregate_identifier VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    event_identifier VARCHAR(255) NOT NULL UNIQUE,
    meta_data MEDIUMBLOB,
    payload MEDIUMBLOB NOT NULL,
    payload_revision VARCHAR(255),
    payload_type VARCHAR(255) NOT NULL,
    time_stamp VARCHAR(255) NOT NULL,
    PRIMARY KEY (aggregate_identifier, sequence_number, type)
);

-- Sample Data for User Service
USE achievesync_user_db;
INSERT IGNORE INTO users (user_id, name, email, password_hash) VALUES 
('demo-user-1', 'Demo User', 'demo@achievesync.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1YzFtYicqABKQQWGZ6vwkZgJIiW6u1i');

INSERT IGNORE INTO consistency_points (user_id, total_points) VALUES 
('demo-user-1', 50);

-- Sample Data for Goal Service
USE achievesync_goal_db;
INSERT IGNORE INTO goals (goal_id, user_id, description, target_date, status, progress_percentage, category) VALUES 
('demo-goal-1', 'demo-user-1', 'Learn Spring Boot Framework', '2025-06-30', 'IN_PROGRESS', 35.0, 'Learning'),
('demo-goal-2', 'demo-user-1', 'Complete Daily Exercise Routine', '2025-12-31', 'IN_PROGRESS', 60.0, 'Health');

INSERT IGNORE INTO goal_tags (goal_id, tag) VALUES 
('demo-goal-1', 'programming'),
('demo-goal-1', 'java'),
('demo-goal-2', 'fitness'),
('demo-goal-2', 'health');