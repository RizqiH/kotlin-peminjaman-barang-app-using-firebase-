-- MyLab QR Scanner Database Schema
-- Database untuk menyimpan data barang laboratorium

-- Create database
CREATE DATABASE IF NOT EXISTS mylab_db;
USE mylab_db;

-- Table untuk menyimpan data barang laboratorium
CREATE TABLE IF NOT EXISTS lab_items (
    id VARCHAR(50) PRIMARY KEY,
    item_code VARCHAR(100) UNIQUE NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    `condition` ENUM('Baik', 'Sedang', 'Rusak') DEFAULT 'Baik',
    location VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_item_code (item_code),
    INDEX idx_category (category),
    INDEX idx_condition (`condition`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table untuk log verifikasi (tracking siapa dan kapan scan barang)
CREATE TABLE IF NOT EXISTS verification_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id VARCHAR(50) NOT NULL,
    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(50) DEFAULT NULL,
    notes TEXT,
    FOREIGN KEY (item_id) REFERENCES lab_items(id) ON DELETE CASCADE,
    INDEX idx_item_id (item_id),
    INDEX idx_verified_at (verified_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data
INSERT INTO lab_items (id, item_code, item_name, category, `condition`, location, description) VALUES
('ITM001', 'LAB-MSC-001', 'Mikroskop Olympus CX23', 'Mikroskop', 'Baik', 'Lab Biologi Lantai 2', 'Mikroskop binokuler dengan pembesaran 40x-1000x'),
('ITM002', 'LAB-BKR-001', 'Beaker Glass 500ml', 'Glassware', 'Baik', 'Lab Kimia Lantai 3', 'Beaker glass borosilikat tahan panas'),
('ITM003', 'LAB-PCR-001', 'PCR Machine Thermal Cycler', 'Alat Molekuler', 'Baik', 'Lab Molekuler Lantai 4', 'Thermal cycler 96 well untuk PCR'),
('ITM004', 'LAB-CEN-001', 'Centrifuge Eppendorf 5424', 'Centrifuge', 'Sedang', 'Lab Mikrobiologi Lantai 2', 'Centrifuge untuk tabung mikro, max 15000 rpm'),
('ITM005', 'LAB-INC-001', 'Incubator Memmert', 'Incubator', 'Baik', 'Lab Mikrobiologi Lantai 2', 'Incubator suhu 20-80°C dengan kapasitas 108L');

-- Insert sample verification logs
INSERT INTO verification_logs (item_id, verified_at, notes) VALUES
('ITM001', '2025-01-15 10:30:00', 'Peminjaman untuk praktikum biologi sel'),
('ITM002', '2025-01-15 11:00:00', 'Peminjaman untuk praktikum kimia dasar'),
('ITM003', '2025-01-15 14:30:00', 'Peminjaman untuk penelitian mahasiswa');

-- View untuk statistik verifikasi
CREATE OR REPLACE VIEW verification_stats AS
SELECT 
    li.id,
    li.item_code,
    li.item_name,
    li.category,
    COUNT(vl.id) as total_verifications,
    MAX(vl.verified_at) as last_verified
FROM lab_items li
LEFT JOIN verification_logs vl ON li.id = vl.item_id
GROUP BY li.id, li.item_code, li.item_name, li.category;

-- Query untuk melihat barang yang paling sering diverifikasi
-- SELECT * FROM verification_stats ORDER BY total_verifications DESC LIMIT 10;

-- Query untuk melihat barang yang belum pernah diverifikasi
-- SELECT * FROM lab_items WHERE id NOT IN (SELECT DISTINCT item_id FROM verification_logs);










