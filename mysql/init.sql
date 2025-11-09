-- Create database if not exists
CREATE DATABASE IF NOT EXISTS equipment_lending;

-- Create user accessible from any host
CREATE USER IF NOT EXISTS 'school_admin'@'%' IDENTIFIED BY 'adminPassword123';

-- Grant all privileges on the database
GRANT ALL PRIVILEGES ON equipment_lending.* TO 'school_admin'@'%';

-- Apply changes
FLUSH PRIVILEGES;
