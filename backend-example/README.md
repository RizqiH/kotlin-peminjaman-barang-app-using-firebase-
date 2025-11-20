# MyLab QR Scanner - Backend API

Backend API untuk sistem verifikasi barang laboratorium menggunakan QR Code.

## Tech Stack

- **Node.js** - Runtime environment
- **Express.js** - Web framework
- **MySQL** - Database
- **mysql2** - MySQL client untuk Node.js

## Setup Instructions

### 1. Install Dependencies

```bash
npm install
```

### 2. Setup Database

Jalankan script SQL untuk membuat database dan tabel:

```bash
mysql -u root -p < database.sql
```

Atau copy-paste isi file `database.sql` ke MySQL client Anda.

### 3. Configure Environment Variables

Copy file `.env.example` menjadi `.env` dan sesuaikan dengan konfigurasi database Anda:

```bash
cp .env.example .env
```

Edit file `.env`:

```env
PORT=8080
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=mylab_db
```

### 4. Run Server

Development mode (dengan auto-reload):
```bash
npm run dev
```

Production mode:
```bash
npm start
```

Server akan berjalan di `http://localhost:8080`

## API Endpoints

### Health Check
```
GET /
Response: { success: true, message: "API is running", version: "1.0.0" }
```

### Get All Items
```
GET /api/items
Response: { success: true, message: "...", data: [...] }
```

### Get Item by ID
```
GET /api/items/:itemId
Response: { success: true, message: "...", data: {...} }
```

### **Verify Item by QR Code** (MAIN ENDPOINT)
```
GET /api/items/verify/:qrCode
Response: { success: true, message: "...", data: {...} }
```

Endpoint ini yang digunakan oleh aplikasi Android untuk verifikasi barang.

### Create Item
```
POST /api/items
Body: {
  "item_code": "LAB-XXX-001",
  "item_name": "Nama Barang",
  "category": "Kategori",
  "condition": "Baik",
  "location": "Lokasi Lab",
  "description": "Deskripsi (optional)"
}
```

### Update Item
```
PUT /api/items/:itemId
Body: { ... same as create ... }
```

### Delete Item
```
DELETE /api/items/:itemId
```

## Database Schema

### Table: lab_items

| Column | Type | Description |
|--------|------|-------------|
| id | VARCHAR(50) | Primary key |
| item_code | VARCHAR(100) | Kode barang (unique) |
| item_name | VARCHAR(255) | Nama barang |
| category | VARCHAR(100) | Kategori barang |
| condition | ENUM | Kondisi: Baik/Sedang/Rusak |
| location | VARCHAR(255) | Lokasi penyimpanan |
| description | TEXT | Deskripsi (optional) |
| created_at | TIMESTAMP | Waktu dibuat |
| updated_at | TIMESTAMP | Waktu diupdate |

### Table: verification_logs

| Column | Type | Description |
|--------|------|-------------|
| id | INT | Primary key (auto increment) |
| item_id | VARCHAR(50) | Foreign key ke lab_items |
| verified_at | TIMESTAMP | Waktu verifikasi |
| user_id | VARCHAR(50) | ID user yang scan (optional) |
| notes | TEXT | Catatan (optional) |

## Integration dengan Android App

Pada aplikasi Android, ubah `BASE_URL` di file `RetrofitInstance.kt`:

```kotlin
private const val BASE_URL = "http://YOUR_SERVER_IP:8080/"
```

Untuk testing lokal dengan emulator:
- Android Emulator: gunakan `http://10.0.2.2:8080/`
- Physical device: gunakan IP address komputer Anda (contoh: `http://192.168.1.100:8080/`)

## Sample Data

Database sudah termasuk 5 sample items:
- ITM001: Mikroskop Olympus CX23
- ITM002: Beaker Glass 500ml
- ITM003: PCR Machine
- ITM004: Centrifuge
- ITM005: Incubator

Anda bisa scan QR Code dengan value "ITM001" sampai "ITM005" untuk testing.

## Security Notes

⚠️ **IMPORTANT**: Ini adalah contoh implementasi untuk development. Untuk production:

1. Tambahkan authentication (JWT tokens)
2. Implement rate limiting
3. Validasi input yang lebih ketat
4. Gunakan HTTPS
5. Implement proper error handling
6. Add logging system
7. Database connection pooling optimization

## License

MIT










