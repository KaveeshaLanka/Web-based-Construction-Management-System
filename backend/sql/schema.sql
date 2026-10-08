-- Run this script once in MySQL to create the database and tables.
-- Usage: mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS lankabuild_cms;
USE lankabuild_cms;

-- ============================
-- USERS TABLE (login / signup)
-- ============================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM(
        'OPERATIONS_MANAGER',
        'PROJECT_MANAGER',
        'SITE_SUPERVISOR',
        'FINANCE_OFFICER',
        'PROCUREMENT_OFFICER',
        'CONSTRUCTION_WORKER'
    ) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- PROJECTS TABLE
-- ============================
CREATE TABLE IF NOT EXISTS projects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    client_name VARCHAR(150),
    location VARCHAR(200),
    start_date DATE,
    end_date DATE,
    budget DECIMAL(15,2),
    status ENUM('PLANNING','IN_PROGRESS','ON_HOLD','COMPLETED','CANCELLED') DEFAULT 'PLANNING',
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ============================
-- PROJECT MILESTONES TABLE
-- ============================
CREATE TABLE IF NOT EXISTS milestones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    due_date DATE,
    status ENUM('PENDING','ACHIEVED','MISSED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- ============================
-- TASKS TABLE
-- ============================
CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    assigned_to INT,
    deadline DATE,
    status ENUM('PENDING','IN_PROGRESS','COMPLETED','DELAYED') DEFAULT 'PENDING',
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);
