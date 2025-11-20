/**
 * Backend API Example untuk MyLab QR Scanner
 * 
 * Server ini menggunakan Node.js + Express + MySQL
 * 
 * Setup Instructions:
 * 1. Install dependencies: npm install express mysql2 cors body-parser dotenv
 * 2. Create .env file dengan DB credentials
 * 3. Run: node server.js
 */

const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const bodyParser = require('body-parser');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8080;

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// MySQL Connection Pool
const pool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'mylab_db',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// Health Check
app.get('/', (req, res) => {
    res.json({
        success: true,
        message: 'MyLab QR Scanner API is running',
        version: '1.0.0'
    });
});

// Get all items
app.get('/api/items', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM lab_items ORDER BY created_at DESC');
        res.json({
            success: true,
            message: 'Items retrieved successfully',
            data: rows
        });
    } catch (error) {
        console.error('Error fetching items:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch items',
            data: null
        });
    }
});

// Get item by ID
app.get('/api/items/:itemId', async (req, res) => {
    try {
        const { itemId } = req.params;
        const [rows] = await pool.query('SELECT * FROM lab_items WHERE id = ?', [itemId]);
        
        if (rows.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'Item not found',
                data: null
            });
        }
        
        res.json({
            success: true,
            message: 'Item retrieved successfully',
            data: rows[0]
        });
    } catch (error) {
        console.error('Error fetching item:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch item',
            data: null
        });
    }
});

// Verify item by QR Code (MAIN ENDPOINT untuk verifikasi)
app.get('/api/items/verify/:qrCode', async (req, res) => {
    try {
        const { qrCode } = req.params;
        
        // QR Code bisa berisi ID item atau kode barang
        // Coba cari berdasarkan ID atau item_code
        const [rows] = await pool.query(
            'SELECT * FROM lab_items WHERE id = ? OR item_code = ?',
            [qrCode, qrCode]
        );
        
        if (rows.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'Barang tidak ditemukan dalam database. Pastikan QR Code valid.',
                data: null
            });
        }
        
        // Log verification untuk tracking
        await pool.query(
            'INSERT INTO verification_logs (item_id, verified_at) VALUES (?, NOW())',
            [rows[0].id]
        );
        
        res.json({
            success: true,
            message: 'Barang berhasil diverifikasi',
            data: rows[0]
        });
    } catch (error) {
        console.error('Error verifying item:', error);
        res.status(500).json({
            success: false,
            message: 'Terjadi kesalahan saat memverifikasi barang',
            data: null
        });
    }
});

// Create new item
app.post('/api/items', async (req, res) => {
    try {
        const { item_code, item_name, category, condition, location, description } = req.body;
        
        if (!item_code || !item_name || !category || !condition || !location) {
            return res.status(400).json({
                success: false,
                message: 'Missing required fields',
                data: null
            });
        }
        
        const [result] = await pool.query(
            'INSERT INTO lab_items (item_code, item_name, category, `condition`, location, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())',
            [item_code, item_name, category, condition, location, description || null]
        );
        
        res.status(201).json({
            success: true,
            message: 'Item created successfully',
            data: {
                id: result.insertId,
                item_code,
                item_name,
                category,
                condition,
                location,
                description
            }
        });
    } catch (error) {
        console.error('Error creating item:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to create item',
            data: null
        });
    }
});

// Update item
app.put('/api/items/:itemId', async (req, res) => {
    try {
        const { itemId } = req.params;
        const { item_code, item_name, category, condition, location, description } = req.body;
        
        const [result] = await pool.query(
            'UPDATE lab_items SET item_code = ?, item_name = ?, category = ?, `condition` = ?, location = ?, description = ?, updated_at = NOW() WHERE id = ?',
            [item_code, item_name, category, condition, location, description || null, itemId]
        );
        
        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: 'Item not found',
                data: null
            });
        }
        
        res.json({
            success: true,
            message: 'Item updated successfully',
            data: null
        });
    } catch (error) {
        console.error('Error updating item:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to update item',
            data: null
        });
    }
});

// Delete item
app.delete('/api/items/:itemId', async (req, res) => {
    try {
        const { itemId } = req.params;
        
        const [result] = await pool.query('DELETE FROM lab_items WHERE id = ?', [itemId]);
        
        if (result.affectedRows === 0) {
            return res.status(404).json({
                success: false,
                message: 'Item not found',
                data: null
            });
        }
        
        res.json({
            success: true,
            message: 'Item deleted successfully',
            data: null
        });
    } catch (error) {
        console.error('Error deleting item:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to delete item',
            data: null
        });
    }
});

// Start server
app.listen(PORT, () => {
    console.log(`🚀 MyLab QR Scanner API running on port ${PORT}`);
    console.log(`📡 API URL: http://localhost:${PORT}`);
});

// Handle graceful shutdown
process.on('SIGTERM', async () => {
    console.log('SIGTERM signal received: closing HTTP server');
    await pool.end();
    process.exit(0);
});










