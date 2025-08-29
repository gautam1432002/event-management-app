-- =====================================================
-- EventTech Database Schema
-- TARUNYAM - Tech Event 2025
-- MySQL 8.0 Compatible
-- =====================================================

-- Create database
CREATE DATABASE IF NOT EXISTS eventtech 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE eventtech;

-- =====================================================
-- Users Table (Admin Authentication)
-- =====================================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- TODO: Implement password hashing (BCrypt)
    role ENUM('admin', 'user') NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_role (role)
);

-- =====================================================
-- Events Table (Event Management)
-- =====================================================
CREATE TABLE events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_event_name (event_name)
);

-- =====================================================
-- Registrations Table (Participant Registration)
-- =====================================================
CREATE TABLE registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    college VARCHAR(200) NOT NULL,
    event VARCHAR(100) NOT NULL,
    winner_status BOOLEAN DEFAULT FALSE,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_event (event),
    INDEX idx_winner_status (winner_status),
    INDEX idx_registration_date (registration_date),
    INDEX idx_email_event (email, event), -- Composite index for duplicate check
    
    FOREIGN KEY (event) REFERENCES events(event_name) ON UPDATE CASCADE
);

-- =====================================================
-- Certificate Log Table (Certificate Tracking)
-- =====================================================
CREATE TABLE certificate_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    registration_id INT NOT NULL,
    certificate_type ENUM('participation', 'winner') NOT NULL,
    certificate_id VARCHAR(100) NOT NULL UNIQUE,
    generated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_registration_id (registration_id),
    INDEX idx_certificate_type (certificate_type),
    INDEX idx_certificate_id (certificate_id),
    INDEX idx_generated_date (generated_date),
    
    FOREIGN KEY (registration_id) REFERENCES registrations(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_reg_type (registration_id, certificate_type)
);

-- =====================================================
-- Audit Log Table (Admin Action Tracking)
-- =====================================================
CREATE TABLE audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT,
    action TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    INDEX idx_admin_id (admin_id),
    INDEX idx_timestamp (timestamp),
    
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =====================================================
-- Insert Default Admin User
-- =====================================================
INSERT INTO users (username, password, role) VALUES 
('admin', 'admin123', 'admin');
-- Note: In production, password should be hashed using BCrypt

-- =====================================================
-- Insert Default Events
-- =====================================================
INSERT INTO events (event_name, description) VALUES 
('Code Rush', 'Speed coding competition with algorithmic challenges to test your programming skills under time pressure'),
('Tech Quiz', 'Test your knowledge in latest technologies, programming languages, and computer science fundamentals'),
('Hackathon', '24-hour coding marathon to build innovative solutions for real-world problems'),
('Web Master', 'Showcase your web development and design skills by creating responsive and interactive websites'),
('Debug Dash', 'Find and fix bugs in given code snippets quickly to demonstrate your debugging expertise'),
('AI Challenge', 'Machine learning and artificial intelligence competition focusing on modern AI techniques');

-- =====================================================
-- Insert Sample Registrations (Optional - Remove in Production)
-- =====================================================
-- Uncomment the following lines if you want sample data for testing
/*
INSERT INTO registrations (name, email, college, event, winner_status) VALUES 
('John Doe', 'john.doe@example.com', 'MIT', 'Code Rush', FALSE),
('Jane Smith', 'jane.smith@stanford.edu', 'Stanford University', 'Hackathon', TRUE),
('Alex Johnson', 'alex.j@berkeley.edu', 'UC Berkeley', 'AI Challenge', FALSE),
('Sarah Williams', 'sarah.w@harvard.edu', 'Harvard University', 'Web Master', TRUE),
('Michael Brown', 'mike.brown@cmu.edu', 'Carnegie Mellon University', 'Tech Quiz', FALSE),
('Emily Davis', 'emily.d@caltech.edu', 'Caltech', 'Debug Dash', FALSE),
('David Wilson', 'david.w@mit.edu', 'MIT', 'AI Challenge', TRUE),
('Lisa Garcia', 'lisa.g@stanford.edu', 'Stanford University', 'Code Rush', FALSE);
*/

-- =====================================================
-- Create Views for Common Queries
-- =====================================================

-- View for participant statistics by event
CREATE VIEW event_statistics AS
SELECT 
    e.event_name,
    e.description,
    COUNT(r.id) as total_registrations,
    COUNT(CASE WHEN r.winner_status = TRUE THEN 1 END) as winners_count,
    COUNT(CASE WHEN r.winner_status = FALSE THEN 1 END) as participants_count
FROM events e
LEFT JOIN registrations r ON e.event_name = r.event
GROUP BY e.id, e.event_name, e.description;

-- View for recent registrations with event details
CREATE VIEW recent_registrations AS
SELECT 
    r.id,
    r.name,
    r.email,
    r.college,
    r.event,
    r.winner_status,
    r.registration_date,
    e.description as event_description
FROM registrations r
JOIN events e ON r.event = e.event_name
ORDER BY r.registration_date DESC;

-- View for certificate statistics
CREATE VIEW certificate_statistics AS
SELECT 
    certificate_type,
    COUNT(*) as count,
    DATE(generated_date) as generation_date
FROM certificate_log
GROUP BY certificate_type, DATE(generated_date)
ORDER BY generation_date DESC;

-- =====================================================
-- Create Stored Procedures for Common Operations
-- =====================================================

-- Procedure to get participant details with certificate info
DELIMITER //
CREATE PROCEDURE GetParticipantDetails(IN participant_id INT)
BEGIN
    SELECT 
        r.id,
        r.name,
        r.email,
        r.college,
        r.event,
        r.winner_status,
        r.registration_date,
        e.description as event_description,
        cl.certificate_type,
        cl.certificate_id,
        cl.generated_date as certificate_generated_date
    FROM registrations r
    JOIN events e ON r.event = e.event_name
    LEFT JOIN certificate_log cl ON r.id = cl.registration_id
    WHERE r.id = participant_id;
END //
DELIMITER ;

-- Procedure to get event summary with registrations
DELIMITER //
CREATE PROCEDURE GetEventSummary()
BEGIN
    SELECT 
        e.event_name,
        e.description,
        COUNT(r.id) as total_registrations,
        COUNT(CASE WHEN r.winner_status = TRUE THEN 1 END) as winners,
        COUNT(cl.id) as certificates_generated,
        MAX(r.registration_date) as last_registration
    FROM events e
    LEFT JOIN registrations r ON e.event_name = r.event
    LEFT JOIN certificate_log cl ON r.id = cl.registration_id
    GROUP BY e.id, e.event_name, e.description
    ORDER BY total_registrations DESC;
END //
DELIMITER ;

-- =====================================================
-- Create Triggers for Data Integrity and Logging
-- =====================================================

-- Trigger to automatically log certificate generation
DELIMITER //
CREATE TRIGGER after_certificate_insert
AFTER INSERT ON certificate_log
FOR EACH ROW
BEGIN
    -- Could add additional logging or notifications here
    -- For example, update a statistics table or send notifications
    UPDATE registrations 
    SET updated_at = CURRENT_TIMESTAMP 
    WHERE id = NEW.registration_id;
END //
DELIMITER ;

-- Trigger to prevent deletion of events with registrations
DELIMITER //
CREATE TRIGGER before_event_delete
BEFORE DELETE ON events
FOR EACH ROW
BEGIN
    DECLARE reg_count INT;
    SELECT COUNT(*) INTO reg_count 
    FROM registrations 
    WHERE event = OLD.event_name;
    
    IF reg_count > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Cannot delete event with existing registrations';
    END IF;
END //
DELIMITER ;

-- =====================================================
-- Create Indexes for Performance Optimization
-- =====================================================

-- Additional composite indexes for complex queries
CREATE INDEX idx_reg_event_winner ON registrations(event, winner_status);
CREATE INDEX idx_reg_date_event ON registrations(registration_date, event);
CREATE INDEX idx_cert_reg_type ON certificate_log(registration_id, certificate_type);

-- Full-text search index for participant names and colleges
ALTER TABLE registrations ADD FULLTEXT(name, college);

-- =====================================================
-- Database Configuration and Optimization
-- =====================================================

-- Optimize tables
OPTIMIZE TABLE users, events, registrations, certificate_log, audit_log;

-- Analyze tables for query optimization
ANALYZE TABLE users, events, registrations, certificate_log, audit_log;

-- =====================================================
-- Create Database User for Application (Production)
-- =====================================================
-- Uncomment and modify for production deployment
/*
-- Create application user with limited privileges
CREATE USER 'eventtech_app'@'localhost' IDENTIFIED BY 'secure_password_here';

-- Grant necessary privileges
GRANT SELECT, INSERT, UPDATE, DELETE ON eventtech.* TO 'eventtech_app'@'localhost';
GRANT EXECUTE ON eventtech.* TO 'eventtech_app'@'localhost';

-- Flush privileges
FLUSH PRIVILEGES;
*/

-- =====================================================
-- Data Validation Constraints
-- =====================================================

-- Add constraints for data validation
ALTER TABLE registrations 
ADD CONSTRAINT chk_email_format 
CHECK (email REGEXP '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$');

ALTER TABLE registrations 
ADD CONSTRAINT chk_name_length 
CHECK (CHAR_LENGTH(TRIM(name)) >= 2 AND CHAR_LENGTH(name) <= 100);

ALTER TABLE registrations 
ADD CONSTRAINT chk_college_length 
CHECK (CHAR_LENGTH(TRIM(college)) >= 2 AND CHAR_LENGTH(college) <= 200);

ALTER TABLE events 
ADD CONSTRAINT chk_event_name_length 
CHECK (CHAR_LENGTH(TRIM(event_name)) >= 3 AND CHAR_LENGTH(event_name) <= 100);

ALTER TABLE events 
ADD CONSTRAINT chk_description_length 
CHECK (CHAR_LENGTH(TRIM(description)) >= 10 AND CHAR_LENGTH(description) <= 1000);

-- =====================================================
-- Show Table Information
-- =====================================================
SELECT 
    'Database Schema Created Successfully!' as Message,
    DATABASE() as Database_Name,
    VERSION() as MySQL_Version,
    NOW() as Created_At;

-- Show table summary
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'Size_MB',
    TABLE_COMMENT
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'eventtech'
ORDER BY TABLE_NAME;

-- =====================================================
-- Database Backup and Maintenance Notes
-- =====================================================
/*
Backup Commands (Run from command line):
1. Full backup:
   mysqldump -u root -p eventtech > eventtech_backup_$(date +%Y%m%d_%H%M%S).sql

2. Data only backup:
   mysqldump -u root -p --no-create-info eventtech > eventtech_data_backup_$(date +%Y%m%d_%H%M%S).sql

3. Structure only backup:
   mysqldump -u root -p --no-data eventtech > eventtech_structure_backup_$(date +%Y%m%d_%H%M%S).sql

Restore Commands:
1. Restore from backup:
   mysql -u root -p eventtech < eventtech_backup_file.sql

Maintenance Commands (Run periodically):
1. Optimize tables:
   OPTIMIZE TABLE registrations, certificate_log, audit_log;

2. Check tables:
   CHECK TABLE registrations, certificate_log, audit_log;

3. Repair tables (if needed):
   REPAIR TABLE table_name;
*/

-- =====================================================
-- Security Notes
-- =====================================================
/*
Security Recommendations:
1. Change default admin password immediately
2. Use environment variables for database credentials
3. Implement proper password hashing (BCrypt) in application
4. Enable SSL/TLS for database connections in production
5. Regularly backup the database
6. Monitor audit_log table for suspicious activities
7. Implement rate limiting for registration endpoints
8. Use prepared statements to prevent SQL injection
9. Validate and sanitize all user inputs
10. Implement proper session management
*/

-- =====================================================
-- End of Schema
-- =====================================================
