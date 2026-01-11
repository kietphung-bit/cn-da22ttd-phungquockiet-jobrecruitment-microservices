<h2 style="text-align: center;">ĐỒ ÁN CHUYÊN NGÀNH</h2>

<h1 style="text-align: center;">XÂY DỰNG WEBSITE TUYỂN DỤNG VÀ TÌM KIẾM VIỆC LÀM </h1>



---



\## Thông Tin Sinh Viên  

\- \*\*Họ và Tên:\*\* Phùng Quốc Kiệt  

\- \*\*MSSV:\*\* 110122101  

\- \*\*Mã lớp:\*\* DA22TTD  

\- \*\*Email sinh viên:\*\* \[110122101@st.tvu.edu.vn](mailto:110122101@st.tvu.edu.vn)  

\- \*\*Email cá nhân:\*\* \[kietphung0209@gmail.com](mailto:kietphung.study@gmail.com)  

\- \*\*Số điện thoại:\*\* 0399283015  



---

## 🎯 Tổng quan hệ thống

**Job Recruitment Platform** là nền tảng tuyển dụng hai chiều, hoạt động như trung gian kết nối:
- **Nhà tuyển dụng (Employer)**: Đăng tin tuyển dụng, tìm kiếm và mời ứng viên
- **Ứng viên (Candidate)**: Tìm việc làm, ứng tuyển, và đăng bài tìm việc chủ động
- **Quản trị viên (Admin)**: Quản lý người dùng, phê duyệt doanh nghiệp, kiểm duyệt nội dung

### Đặc điểm nổi bật:
- ✅ **Mô hình Post-moderation**: Admin xử lý báo cáo/vi phạm sau khi nội dung được đăng
- 🔐 **Bảo mật cao**: JWT Authentication, Spring Security, CORS, Rate Limiting
- 🎨 **Giao diện hiện đại**: React 19 + TailwindCSS + Lucide React Icons
- 📝 **Rich Text Editor**: TipTap editor cho mô tả công việc và giới thiệu bản thân
- 🇻🇳 **Địa phương hóa**: Hỗ trợ 63 tỉnh thành Việt Nam, định dạng tiền tệ VND

---

## 🏗️ Kiến trúc dự án

```
┌─────────────────┐
│   React SPA     │  ← Frontend (Vite + React 19)
│   Port: 5173    │
└────────┬────────┘
         │ HTTP/HTTPS
         │
┌────────▼────────┐
│  Spring Boot    │  ← Backend API (Java 21 + Spring Boot 4.0)
│   Port: 8080    │
└────────┬────────┘
         │ JDBC
         │
┌────────▼────────┐
│   PostgreSQL    │  ← Database
│   Port: 5432    │
└─────────────────┘
```

### Kiến trúc Backend (Layered Architecture)

```
Controller Layer (REST API Endpoints)
         ↓
Service Layer (Business Logic)
         ↓
Repository Layer (Data Access - Spring Data JPA)
         ↓
Database (PostgreSQL)
```

### Kiến trúc Frontend (Component-Based)

```
Pages (Feature Components)
    ↓
Layouts (Admin/Company/Public)
    ↓
Components (Reusable UI)
    ↓
Services (API Calls)
    ↓
Contexts (Auth, State Management)
```

---

## 💻 Công nghệ sử dụng

### Backend
- **Framework**: Spring Boot 4.0.0 (Java 21)
- **Security**: Spring Security + JWT (Stateless Authentication)
- **ORM**: Spring Data JPA + Hibernate
- **Database**: PostgreSQL 16+
- **Validation**: Bean Validation (Hibernate Validator)
- **Build Tool**: Maven
- **Additional Libraries**:
  - `jjwt`: JWT token generation/validation
  - `jackson`: JSON processing
  - `lombok`: Reduce boilerplate code

### Frontend
- **Framework**: React 19.2.3
- **Build Tool**: Vite 7.2.4
- **Language**: JavaScript (ES6+) + TypeScript 5.9
- **UI Framework**: TailwindCSS 3.4.19
- **Routing**: React Router v7.11.0
- **Forms**: React Hook Form + Yup validation
- **HTTP Client**: Axios 1.13.2
- **Rich Text**: TipTap Editor 3.14.0
- **Icons**: Lucide React 0.562.0
- **Notifications**: React Hot Toast 2.6.0
- **Charts**: Recharts 3.6.0

---

## 👥 Tính năng theo vai trò

### 🔴 Quản trị viên (Admin - ADM)

**Dashboard & Analytics**
- 📊 Thống kê tổng quan hệ thống (người dùng, việc làm, ứng tuyển)
- 📈 Biểu đồ phân tích xu hướng tuyển dụng

**Quản lý người dùng**
- 👤 Xem danh sách tất cả người dùng (Doanh nghiệp + Ứng viên)
- 🔒 Khóa/Mở khóa tài khoản
- 📧 Xem thông tin liên hệ

**Phê duyệt doanh nghiệp**
- 🏢 Xem danh sách doanh nghiệp chờ duyệt (PENDING)
- ✅ Phê duyệt/Từ chối đăng ký doanh nghiệp
- 🔍 Xem chi tiết hồ sơ doanh nghiệp
- 🚫 Chuyển trạng thái ACTIVE ↔ BLOCKED

**Quản lý tuyển dụng**
- 📋 Xem tất cả tin tuyển dụng và bài tìm việc
- 🗑️ Xóa tin vi phạm chính sách
- 👁️ Xem chi tiết bài đăng

**Quản lý danh mục**
- 🏷️ CRUD Job Categories (Ngành nghề)
- 💰 Thiết lập mức lương tham khảo

---

### 🔵 Nhà tuyển dụng (Employer - DN)

**Dashboard**
- 📊 Thống kê tin tuyển dụng (Đang mở, Đóng, Tổng ứng tuyển)
- 📈 Biểu đồ phân tích hiệu suất tuyển dụng

**Quản lý tin tuyển dụng**
- ➕ Tạo tin tuyển dụng mới (Rich Text Editor)
- 📝 Chỉnh sửa/Xóa tin đã đăng
- 👁️ Xem chi tiết và danh sách ứng tuyển
- 🔄 Thay đổi trạng thái (WAIT/ACTIVE/CLOSED/HIDDEN)
- 📍 Chọn địa điểm từ 63 tỉnh thành VN

**Quản lý ứng tuyển**
- 📬 Xem danh sách ứng viên đã ứng tuyển
- 📄 Tải và xem CV
- ✅❌ Phê duyệt/Từ chối ứng viên
- 📊 Lọc theo trạng thái (PENDING/APPROVED/REJECTED)

**Tìm kiếm ứng viên**
- 🔍 Tìm ứng viên theo kỹ năng, địa điểm, mức lương
- 📩 Gửi lời mời ứng tuyển chủ động
- 👁️ Xem hồ sơ chi tiết ứng viên
- 🎯 Bộ lọc thông minh (Skills, Location, Salary)

---

### 🟢 Ứng viên (Candidate - UV)

**Tìm kiếm việc làm**
- 🔍 Tìm kiếm theo từ khóa, địa điểm, mức lương
- 🏢 Xem chi tiết công ty và mô tả công việc
- ⭐ Lưu tin tuyển dụng yêu thích
- 📋 Lọc theo ngành nghề, kinh nghiệm

**Ứng tuyển**
- 📤 Nộp hồ sơ ứng tuyển (Upload CV PDF/DOCX)
- 📊 Theo dõi trạng thái ứng tuyển
- 📧 Nhận thông báo kết quả

**Quản lý CV**
- ➕ Upload nhiều phiên bản CV
- 👁️ Xem/Tải lại CV đã upload
- 🔄 Đặt trạng thái ACTIVE/HIDDEN
- 🗑️ Xóa CV không dùng

**Đăng bài tìm việc (Job Seeking Post)**
- ➕ Tạo bài tìm việc chủ động (Rich Text)
- 📝 Giới thiệu bản thân, kỹ năng, mức lương mong muốn
- 🔄 Chỉnh sửa/Ẩn/Xóa bài đăng
- 📍 Chọn địa điểm làm việc mong muốn
- ⏰ Đặt thời gian hết hạn

**Lời mời từ nhà tuyển dụng**
- 📬 Nhận lời mời ứng tuyển từ công ty
- 👁️ Xem chi tiết lời mời
- ✅❌ Chấp nhận/Từ chối lời mời

---

### 🌐 Người dùng chưa đăng nhập (Guest)

- 🔍 Tìm kiếm và xem tin tuyển dụng công khai
- 🏢 Xem thông tin công ty
- 👤 Xem bài tìm việc của ứng viên (Public Talent Search)
- 📊 Lọc theo kỹ năng, địa điểm, mức lương
- 🔐 Đăng ký tài khoản (Employer/Candidate)

---

## ⚙️ Yêu cầu hệ thống

### Phần mềm cần cài đặt

| Phần mềm | Version | Mục đích |
|----------|---------|----------|
| **Java JDK** | 21+ | Chạy Spring Boot backend |
| **Node.js** | 18+ | Build và chạy React frontend |
| **PostgreSQL** | 16+ | Database chính |
| **Maven** | 3.9+ | Build backend (hoặc dùng Maven Wrapper) |
| **Git** | 2.40+ | Version control |

---

## 🚀 Cài đặt và chạy dự án

### 1️⃣ Clone dự án

```bash
git clone <repository-url>
cd JobRecruitment
```

### 2️⃣ Cấu hình Database

**Tạo database PostgreSQL:**

```sql
CREATE DATABASE jobrecruitment;
CREATE USER jobuser WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE jobrecruitment TO jobuser;
```

### 3️⃣ Cấu hình Backend

**a. Copy file cấu hình mẫu:**

```bash
cd backend/src/main/resources
cp application-dev.properties.example application-dev.properties
```

**b. Chỉnh sửa `application-dev.properties`:**

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/jobrecruitment
spring.datasource.username=jobuser
spring.datasource.password=your_password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret=your_super_secret_jwt_key_at_least_256_bits_long
jwt.expiration=86400000

# Server Port
server.port=8080

# File Upload
file.upload-dir=./uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# CORS
cors.allowed-origins=http://localhost:5173
```

> **⚠️ Lưu ý**: File `application-dev.properties` nằm trong `.gitignore` để bảo mật thông tin nhạy cảm.

**c. Build và chạy backend:**

```bash
cd backend

# Option 1: Sử dụng Maven Wrapper (Recommended)
./mvnw clean install
./mvnw spring-boot:run

# Option 2: Sử dụng Maven đã cài đặt
mvn clean install
mvn spring-boot:run
```

Backend sẽ chạy tại: **http://localhost:8080**

### 4️⃣ Cấu hình Frontend

**a. Copy file environment mẫu:**

```bash
cd frontend
cp .env.example .env
```

**b. Chỉnh sửa `.env`:**

```env
# Backend API URL
VITE_API_BASE_URL=http://localhost:8080/api

# App Configuration
VITE_APP_NAME=Job Recruitment Platform
VITE_APP_VERSION=1.0.0

# File Upload
VITE_MAX_FILE_SIZE=10485760
```

> **⚠️ Lưu ý**: File `.env` nằm trong `.gitignore` để tránh commit thông tin môi trường.

**c. Cài đặt dependencies và chạy:**

```bash
cd frontend

# Cài đặt packages
npm install

# Chạy development server
npm run dev
```

Frontend sẽ chạy tại: **http://localhost:5173**

### 5️⃣ Truy cập ứng dụng

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **Swagger UI** (nếu có): http://localhost:8080/swagger-ui.html

### 6️⃣ Tài khoản mặc định

**Admin (Seeded Account):**
```
Username: admin@system.com
Password: admin123
UserCode: AD00000001
Role: ADM
```

**Tạo tài khoản mới:**
- Truy cập trang đăng ký: http://localhost:5173/register
- Chọn vai trò: Nhà tuyển dụng (Employer) hoặc Ứng viên (Candidate)
- Điền thông tin và submit

> **⚠️ Lưu ý**: Tài khoản Employer cần được Admin phê duyệt trước khi sử dụng đầy đủ tính năng.

---

## 📁 Cấu trúc thư mục

### Backend (Spring Boot)

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/jobrecruitment/backend/
│   │   │   ├── config/              # Cấu hình (Security, CORS, JWT)
│   │   │   ├── controller/          # REST API Endpoints
│   │   │   │   ├── admin/           # Admin endpoints
│   │   │   │   ├── company/         # Employer endpoints
│   │   │   │   ├── candidate/       # Candidate endpoints
│   │   │   │   └── public/          # Public endpoints
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── request/         # Request DTOs
│   │   │   │   └── response/        # Response DTOs
│   │   │   ├── entity/              # JPA Entities (Database Models)
│   │   │   ├── repository/          # Spring Data JPA Repositories
│   │   │   ├── service/             # Business Logic Layer
│   │   │   │   └── impl/            # Service Implementations
│   │   │   ├── security/            # JWT Filter, UserDetails
│   │   │   ├── exception/           # Custom Exceptions & Handlers
│   │   │   └── util/                # Utility Classes
│   │   └── resources/
│   │       ├── application.properties           # Main config
│   │       ├── application-dev.properties.example  # Dev config template
│   │       └── data.sql             # Initial data seeding (optional)
│   └── test/                        # Unit & Integration Tests
├── uploads/                         # File upload directory (CV, Logo)
├── pom.xml                          # Maven dependencies
└── README.md                        # Backend documentation
```

### Frontend (React + Vite)

```
frontend/
├── public/                          # Static assets
├── src/
│   ├── components/                  # React Components
│   │   ├── common/                  # Shared components (Button, Input...)
│   │   ├── features/                # Feature-specific components
│   │   │   ├── auth/                # Login, Register
│   │   │   ├── job/                 # Job cards, details
│   │   │   ├── talent/              # Talent search components
│   │   │   └── application/         # Application management
│   │   ├── guards/                  # Route guards (RoleGuard)
│   │   └── layout/                  # Layouts (AdminLayout, CompanyLayout)
│   ├── contexts/                    # React Context (AuthContext)
│   ├── data/                        # Static data (provinces.js)
│   ├── pages/                       # Page components
│   │   ├── admin/                   # Admin pages
│   │   ├── company/                 # Employer pages
│   │   ├── candidate/               # Candidate pages
│   │   └── public/                  # Public pages
│   ├── services/                    # API service calls (Axios)
│   ├── utils/                       # Helper functions
│   ├── App.jsx                      # Root component
│   ├── main.jsx                     # Entry point
│   └── index.css                    # Global styles (TailwindCSS)
├── .env.example                     # Environment template
├── vite.config.js                   # Vite configuration
├── tailwind.config.js               # TailwindCSS configuration
├── package.json                     # NPM dependencies
└── README.md                        # Frontend documentation
```

---

## 📚 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Đăng ký tài khoản mới | ❌ |
| POST | `/api/auth/login` | Đăng nhập | ❌ |
| POST | `/api/auth/refresh` | Refresh JWT token | ✅ |
| GET | `/api/auth/profile` | Lấy thông tin người dùng | ✅ |

### Admin Endpoints

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| GET | `/api/admin/users` | Danh sách người dùng | ADM |
| PATCH | `/api/admin/users/{id}/toggle-status` | Khóa/Mở khóa user | ADM |
| GET | `/api/admin/companies/pending` | DS công ty chờ duyệt | ADM |
| PATCH | `/api/admin/companies/{id}/approve` | Phê duyệt công ty | ADM |
| GET | `/api/admin/dashboard/statistics` | Thống kê tổng quan | ADM |

### Employer Endpoints

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| GET | `/api/company/jobs` | Danh sách tin tuyển dụng | DN |
| POST | `/api/company/jobs` | Tạo tin tuyển dụng | DN |
| PUT | `/api/company/jobs/{id}` | Cập nhật tin tuyển dụng | DN |
| DELETE | `/api/company/jobs/{id}` | Xóa tin tuyển dụng | DN |
| GET | `/api/company/applications` | DS ứng tuyển | DN |
| PATCH | `/api/company/applications/{id}/approve` | Duyệt ứng tuyển | DN |
| GET | `/api/company/seeking-posts` | Tìm ứng viên | DN |

### Candidate Endpoints

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| GET | `/api/candidate/jobs` | Tìm việc làm | UV |
| POST | `/api/candidate/applications` | Ứng tuyển | UV |
| GET | `/api/candidate/applications` | DS ứng tuyển của tôi | UV |
| POST | `/api/candidate/cv/upload` | Upload CV | UV |
| GET | `/api/candidate/cv` | Danh sách CV | UV |
| POST | `/api/candidate/seeking-posts` | Tạo bài tìm việc | UV |
| GET | `/api/candidate/seeking-posts` | DS bài tìm việc của tôi | UV |

### Public Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/public/jobs` | Tìm việc làm (public) | ❌ |
| GET | `/api/public/jobs/{id}` | Chi tiết việc làm | ❌ |
| GET | `/api/public/seeking-posts` | Bài tìm việc (public) | ❌ |
| GET | `/api/public/companies/{id}` | Thông tin công ty | ❌ |

> **📖 Chi tiết API**: Xem thêm trong file `backend/API_REFERENCE.md`

---

## 🔐 Authentication Flow

### JWT Token-Based Authentication

```
1. User Login
   ↓
2. Backend validates credentials
   ↓
3. Generate JWT Token (Access Token)
   ↓
4. Return token to Frontend
   ↓
5. Frontend stores token (localStorage)
   ↓
6. Frontend sends token in Authorization header
   ↓
7. Backend validates token on each request
```

**Token Structure:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Token Payload:**
```json
{
  "sub": "user@example.com",
  "userId": 123,
  "role": "DN",
  "iat": 1673640000,
  "exp": 1673726400
}
```

---

## 🧪 Testing

### Backend Testing

```bash
cd backend

# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=AuthControllerTest

# Generate coverage report
./mvnw jacoco:report
```

### Frontend Testing

```bash
cd frontend

# Run tests (if configured)
npm run test

# E2E testing with Playwright/Cypress
npm run test:e2e
```
