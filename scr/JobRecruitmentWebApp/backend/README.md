# 🚀 Job Recruitment Platform - Backend API

**Hệ thống Backend RESTful API cho Nền tảng Tuyển dụng Việc làm**

[![Java](https://img.shields.io/badge/Java-21-red?style=flat&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen?style=flat&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-orange?style=flat&logo=apache-maven)](https://maven.apache.org/)

---

## 📋 Mục Lục

- [Giới Thiệu](#-giới-thiệu)
- [Công Nghệ Sử Dụng](#-công-nghệ-sử-dụng)
- [Yêu Cầu Hệ Thống](#-yêu-cầu-hệ-thống)
- [Cài Đặt & Chạy (Local - JDK)](#-cài-đặt--chạy-local---jdk)
- [Cài Đặt & Chạy (Docker/Production)](#-cài-đặt--chạy-dockerproduction)
- [Cấu Trúc Dự Án](#-cấu-trúc-dự-án)
- [Tài Liệu API](#-tài-liệu-api)
- [Tài Khoản Mặc Định](#-tài-khoản-mặc-định)
- [Kiểm Thử Với Postman](#-kiểm-thử-với-postman)
- [Môi Trường & Cấu Hình](#-môi-trường--cấu-hình)
- [Bảo Mật](#-bảo-mật)
- [Đóng Góp](#-đóng-góp)
- [Giấy Phép](#-giấy-phép)

---

## 📖 Giới Thiệu

**Job Recruitment Platform** là một hệ thống tuyển dụng toàn diện, kết nối giữa **Nhà tuyển dụng** và **Ứng viên**. Hệ thống được xây dựng theo chuẩn **RESTful API**, có khả năng mở rộng (microservices-ready) và tuân thủ các best practices về bảo mật, hiệu suất.

### ✨ Tính Năng Chính

#### 🏢 **Dành cho Nhà Tuyển Dụng (Employer)**
- ✅ Đăng ký tài khoản công ty
- ✅ Quản lý hồ sơ công ty (logo, mô tả, website)
- ✅ Đăng tin tuyển dụng (CRUD)
- ✅ Xem danh sách ứng viên ứng tuyển
- ✅ Duyệt/từ chối hồ sơ ứng tuyển
- ✅ Lọc ứng viên theo trạng thái

#### 👤 **Dành cho Ứng Viên (Candidate)**
- ✅ Đăng ký tài khoản cá nhân
- ✅ Quản lý hồ sơ (học vấn, kinh nghiệm, kỹ năng)
- ✅ Tải lên & quản lý CV (nhiều phiên bản)
- ✅ Tìm kiếm việc làm (lương, địa điểm, ngành nghề)
- ✅ Ứng tuyển vào công việc
- ✅ Lưu việc làm yêu thích (Bookmark)
- ✅ Theo dõi trạng thái đơn ứng tuyển

#### 🛡️ **Dành cho Quản Trị Viên (Admin)**
- ✅ Quản lý người dùng (khóa/mở khóa tài khoản)
- ✅ Kiểm duyệt tin tuyển dụng
- ✅ Quản lý danh mục ngành nghề (CRUD)
- ✅ Xem thống kê tổng quan hệ thống
- ✅ Kiểm duyệt công ty mới đăng ký

---

## 🛠️ Công Nghệ Sử Dụng

| **Technology** | **Version** | **Mô Tả** |
|----------------|-------------|-----------|
| Java | 21 | Ngôn ngữ lập trình chính |
| Spring Boot | 4.0.0 | Framework backend |
| Spring Security | 6.x | Bảo mật & JWT Authentication |
| Spring Data JPA | 3.x | ORM và truy vấn database |
| PostgreSQL | 16+ | Cơ sở dữ liệu quan hệ |
| Maven | 3.9+ | Quản lý dependencies |
| Springdoc OpenAPI | 2.x | Swagger UI documentation |
| Lombok | 1.18+ | Giảm boilerplate code |
| BCrypt | - | Mã hóa mật khẩu |

### 📦 Thư Viện Chính

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
    </dependency>
    
    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
</dependencies>
```

---

## 💻 Yêu Cầu Hệ Thống

### 🔧 **Development (Local)**
- ✅ **JDK 21** (hoặc cao hơn) - [Download](https://www.oracle.com/java/technologies/downloads/)
- ✅ **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- ✅ **PostgreSQL 16+** - [Download](https://www.postgresql.org/download/)
- ✅ **Git** - [Download](https://git-scm.com/downloads)
- ✅ IDE (IntelliJ IDEA, Eclipse, VS Code)

### 🐳 **Production (Docker)**
- ✅ **Docker 24+** - [Download](https://www.docker.com/get-started)
- ✅ **Docker Compose 2.x** - Thường đi kèm với Docker Desktop

---

## 🚀 Cài Đặt & Chạy (Local - JDK)

### **Bước 1: Clone Repository**

```bash
git clone https://github.com/your-username/JobRecruitment.git
cd JobRecruitment/backend
```

### **Bước 2: Chuẩn Bị Database**

#### 2.1. Tạo Database PostgreSQL

```bash
# Đăng nhập PostgreSQL
psql -U postgres

# Tạo database
CREATE DATABASE jobrecruitment_db;

# Thoát
\q
```

#### 2.2. Chạy Migration (Tùy chọn)

Nếu có file SQL backup, import vào database:

```bash
psql -U postgres -d jobrecruitment_db -f database/schema.sql
```

### **Bước 3: Cấu Hình Application**

#### 3.1. Tạo File Cấu Hình

```bash
cd src/main/resources
cp application-dev.properties.example application-dev.properties
```

#### 3.2. Chỉnh Sửa `application-dev.properties`

```properties
# Database Connection (Thay đổi theo môi trường của bạn)
spring.datasource.url=jdbc:postgresql://localhost:5432/jobrecruitment_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD

# JWT Secret (Dev only - có thể dùng giá trị bất kỳ)
jwt.secret=DevSecretKeyForLocalTesting123456789
jwt.expiration=86400000
```

### **Bước 4: Build & Run**

#### Cách 1: Chạy Trực Tiếp (Khuyến nghị cho Dev)

```bash
# Build project
mvn clean install -DskipTests

# Chạy application
mvn spring-boot:run
```

#### Cách 2: Build JAR và Chạy

```bash
# Build JAR file
mvn clean package -DskipTests

# Chạy JAR
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### **Bước 5: Xác Nhận Ứng Dụng Chạy**

Mở trình duyệt và truy cập:

- **Health Check**: http://localhost:8080/api/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/v3/api-docs

Nếu thấy Swagger UI hiển thị danh sách API → **Thành công!** 🎉

---

## 🐳 Cài Đặt & Chạy (Docker/Production)

### **Bước 1: Tạo File docker-compose.yml**

Tạo file `docker-compose.yml` trong thư mục root:

```yaml
version: '3.8'

services:
  # PostgreSQL Database
  postgres:
    image: postgres:16-alpine
    container_name: jobrecruitment_db
    environment:
      POSTGRES_DB: jobrecruitment_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - jobrecruitment_network
    restart: unless-stopped

  # Spring Boot Backend
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: jobrecruitment_backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: jobrecruitment_db
      DB_USERNAME: postgres
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: 86400000
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    networks:
      - jobrecruitment_network
    restart: unless-stopped

volumes:
  postgres_data:

networks:
  jobrecruitment_network:
    driver: bridge
```

### **Bước 2: Tạo Dockerfile**

Tạo file `Dockerfile` trong thư mục `backend/`:

```dockerfile
# Build Stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **Bước 3: Tạo File Environment Variables**

Tạo file `.env` trong thư mục root (KHÔNG commit file này lên GitHub):

```env
# Database Password
DB_PASSWORD=your_strong_database_password_here

# JWT Secret (Generate with: openssl rand -base64 64)
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here
```

**Lưu ý**: Thêm `.env` vào `.gitignore` để không commit secrets!

### **Bước 4: Chạy Docker Compose**

```bash
# Build và khởi động containers
docker-compose up -d

# Xem logs
docker-compose logs -f backend

# Dừng containers
docker-compose down

# Dừng và xóa volumes (reset database)
docker-compose down -v
```

### **Bước 5: Xác Nhận Deployment**

Kiểm tra ứng dụng đã chạy:

```bash
# Kiểm tra containers đang chạy
docker ps

# Test health endpoint
curl http://localhost:8080/api/health

# Truy cập Swagger UI
# http://localhost:8080/swagger-ui.html
```

---

## 📁 Cấu Trúc Dự Án

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/jobrecruitment/backend/
│   │   │   ├── configs/               # Cấu hình (Security, Swagger, CORS)
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── DataSeeder.java
│   │   │   │
│   │   │   ├── controllers/           # REST API Controllers
│   │   │   │   ├── AuthControllerV1.java
│   │   │   │   ├── JobControllerV1.java
│   │   │   │   ├── ApplicationControllerV1.java
│   │   │   │   ├── CompanyControllerV1.java
│   │   │   │   ├── CandidateControllerV1.java
│   │   │   │   ├── CVControllerV1.java
│   │   │   │   ├── SavedJobControllerV1.java
│   │   │   │   ├── JobCategoryControllerV1.java
│   │   │   │   ├── AdminControllerV1.java
│   │   │   │   └── HealthController.java
│   │   │   │
│   │   │   ├── dtos/                  # Data Transfer Objects
│   │   │   │   ├── request/           # Request DTOs (Input)
│   │   │   │   └── response/          # Response DTOs (Output)
│   │   │   │
│   │   │   ├── entities/              # JPA Entities (Database Models)
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Company.java
│   │   │   │   ├── Candidate.java
│   │   │   │   ├── Job.java
│   │   │   │   ├── Application.java
│   │   │   │   ├── CV.java
│   │   │   │   ├── SavedJob.java
│   │   │   │   └── JobCategory.java
│   │   │   │
│   │   │   ├── enums/                 # Enum Types
│   │   │   │   ├── JobStatus.java
│   │   │   │   ├── ApplicationStatus.java
│   │   │   │   ├── CompanyStatus.java
│   │   │   │   ├── CVStatus.java
│   │   │   │   └── Gender.java
│   │   │   │
│   │   │   ├── exceptions/            # Exception Handling
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── ValidationException.java
│   │   │   │   └── AccessDeniedException.java
│   │   │   │
│   │   │   ├── filters/               # Security Filters
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   │
│   │   │   ├── mappers/               # Entity ↔ DTO Mappers
│   │   │   │   ├── JobMapper.java
│   │   │   │   ├── ApplicationMapper.java
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── repositories/          # JPA Repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── JobRepository.java
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── services/              # Business Logic
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── JobServiceV1.java
│   │   │   │   ├── impl/              # Service Implementations
│   │   │   │   │   ├── JobServiceV1Impl.java
│   │   │   │   │   └── ...
│   │   │   │
│   │   │   ├── specifications/        # JPA Specifications (Dynamic Queries)
│   │   │   │   ├── JobSpecifications.java
│   │   │   │   └── ApplicationSpecifications.java
│   │   │   │
│   │   │   ├── utils/                 # Utility Classes
│   │   │   │   ├── JwtUtils.java
│   │   │   │   ├── CodeGenerator.java
│   │   │   │   └── DateUtils.java
│   │   │   │
│   │   │   ├── validators/            # Custom Validators
│   │   │   │   ├── WorkingAgeValidator.java
│   │   │   │   └── ValidDateRange.java
│   │   │   │
│   │   │   └── BackendApplication.java  # Main Application Class
│   │   │
│   │   └── resources/
│   │       ├── application.properties           # Main config
│   │       ├── application-dev.properties       # Dev profile (gitignored)
│   │       ├── application-prod.properties      # Prod profile (gitignored)
│   │       └── application.properties.example   # Template (for GitHub)
│   │
│   └── test/
│       └── java/                      # Unit & Integration Tests
│
├── .gitignore                         # Git ignore rules
├── Dockerfile                         # Docker build instructions
├── pom.xml                            # Maven dependencies
└── README.md                          # This file
```

---

## 📚 Tài Liệu API

### **🌐 Swagger UI (Khuyến nghị)**

Sau khi chạy ứng dụng, truy cập:

```
http://localhost:8080/swagger-ui.html
```

Swagger UI cung cấp:
- ✅ Danh sách đầy đủ 42 API endpoints
- ✅ Mô tả chi tiết từng endpoint (parameters, responses)
- ✅ Thử nghiệm API trực tiếp trên trình duyệt
- ✅ Authentication (Bearer Token) hỗ trợ sẵn

### **📖 OpenAPI JSON Specification**

```
http://localhost:8080/v3/api-docs
```

### **📋 API Endpoints Overview**

#### **🔐 Authentication (`/api/v1/auth`)**
| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| POST | `/api/v1/auth/login` | Đăng nhập (trả về JWT token) | ❌ Public |
| POST | `/api/v1/auth/register-company` | Đăng ký tài khoản nhà tuyển dụng | ❌ Public |
| POST | `/api/v1/auth/register-candidate` | Đăng ký tài khoản ứng viên | ❌ Public |

#### **💼 Jobs (`/api/v1/jobs`)**
| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| GET | `/api/v1/jobs` | Danh sách việc làm (pagination, filter) | ❌ Public |
| POST | `/api/v1/jobs` | Đăng tin tuyển dụng | ✅ Employer |
| GET | `/api/v1/jobs/{id}` | Chi tiết việc làm | ❌ Public |
| PUT | `/api/v1/jobs/{id}` | Cập nhật tin tuyển dụng | ✅ Employer |
| DELETE | `/api/v1/jobs/{id}` | Xóa tin tuyển dụng (soft delete) | ✅ Employer |
| GET | `/api/v1/jobs/me` | Danh sách tin đã đăng | ✅ Employer |
| PATCH | `/api/v1/jobs/{id}/status` | Thay đổi trạng thái tin | ✅ Employer |

#### **📄 Applications (`/api/v1/applications`)**
| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| POST | `/api/v1/applications` | Ứng tuyển việc làm | ✅ Candidate |
| GET | `/api/v1/applications/me` | Đơn ứng tuyển của tôi (pagination) | ✅ Candidate |
| GET | `/api/v1/applications/job/{id}` | Danh sách ứng viên (pagination) | ✅ Employer |
| PATCH | `/api/v1/applications/{id}/status` | Duyệt/từ chối hồ sơ | ✅ Employer |

#### **🏢 Companies (`/api/v1/companies`)**
| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| GET | `/api/v1/companies/me` | Hồ sơ công ty của tôi | ✅ Employer |
| PUT | `/api/v1/companies/me` | Cập nhật hồ sơ công ty | ✅ Employer |

#### **👤 Candidates (`/api/v1/candidates`)**
| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| GET | `/api/v1/candidates/me` | Hồ sơ cá nhân của tôi | ✅ Candidate |
| PUT | `/api/v1/candidates/me` | Cập nhật hồ sơ cá nhân | ✅ Candidate |

#### **📎 CVs (`/api/v1/cvs`)**
| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| POST | `/api/v1/cvs` | Upload CV mới | ✅ Candidate |
| GET | `/api/v1/cvs/me` | Danh sách CV của tôi | ✅ Candidate |
| PATCH | `/api/v1/cvs/{id}/status` | Ẩn/hiện CV | ✅ Candidate |
| DELETE | `/api/v1/cvs/{id}` | Xóa CV (soft delete) | ✅ Candidate |

#### **⭐ Saved Jobs (`/api/v1/saved-jobs`)**
| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| POST | `/api/v1/saved-jobs` | Lưu việc làm yêu thích | ✅ Candidate |
| GET | `/api/v1/saved-jobs/me` | Danh sách việc đã lưu (pagination) | ✅ Candidate |
| DELETE | `/api/v1/saved-jobs/{jobId}` | Bỏ lưu việc làm | ✅ Candidate |

#### **📂 Job Categories (`/api/v1/categories`)**
| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| GET | `/api/v1/categories` | Danh sách ngành nghề | ❌ Public |
| POST | `/api/v1/categories` | Tạo danh mục mới | ✅ Admin |
| PUT | `/api/v1/categories/{id}` | Cập nhật danh mục | ✅ Admin |
| DELETE | `/api/v1/categories/{id}` | Xóa danh mục | ✅ Admin |

#### **🛡️ Admin (`/api/v1/admin`)**
| Method | Endpoint | Mô Tả | Auth |
|--------|----------|-------|------|
| GET | `/api/v1/admin/dashboard/stats` | Thống kê hệ thống | ✅ Admin |
| GET | `/api/v1/admin/users` | Danh sách người dùng (pagination, filter) | ✅ Admin |
| PATCH | `/api/v1/admin/users/{id}/lock` | Khóa tài khoản | ✅ Admin |
| PATCH | `/api/v1/admin/users/{id}/unlock` | Mở khóa tài khoản | ✅ Admin |
| PATCH | `/api/v1/admin/companies/{id}/status` | Kiểm duyệt công ty | ✅ Admin |
| PATCH | `/api/v1/admin/jobs/{id}/status` | Kiểm duyệt tin tuyển dụng | ✅ Admin |

---

## 🔑 Tài Khoản Mặc Định

Hệ thống tự động tạo các tài khoản mẫu khi chạy lần đầu (DataSeeder):

### **👨‍💼 Admin**
```
Email: admin@jobrecruitment.com
Password: Admin@123
Role: Administrator
```

### **🏢 Employer (Nhà Tuyển Dụng)**
```
Email: employer@fpt.com
Password: Employer@123
Company: FPT Software
Role: Employer
```

### **👤 Candidate (Ứng Viên)**
```
Email: candidate@gmail.com
Password: Candidate@123
Name: Nguyễn Văn A
Role: Candidate
```

**Lưu ý**: Đổi mật khẩu sau khi đăng nhập lần đầu nếu deploy lên production!

---

## 📬 Kiểm Thử Với Postman

### **📥 Import Postman Collection**

1. Mở Postman Desktop hoặc Web App
2. Click **Import** → **Link** → Nhập URL:
   ```
   http://localhost:8080/v3/api-docs
   ```
3. Hoặc import file JSON thủ công (tạo collection từ Swagger)

### **🔗 Postman Collection Structure (Skeleton)**

```json
{
  "info": {
    "name": "Job Recruitment Platform API",
    "description": "Complete RESTful API cho hệ thống tuyển dụng việc làm",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "auth": {
    "type": "bearer",
    "bearer": [
      {
        "key": "token",
        "value": "{{jwt_token}}",
        "type": "string"
      }
    ]
  },
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080",
      "type": "string"
    },
    {
      "key": "jwt_token",
      "value": "",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "1. Authentication",
      "item": [
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "url": "{{base_url}}/api/v1/auth/login",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"admin@jobrecruitment.com\",\n  \"password\": \"Admin@123\"\n}"
            }
          }
        },
        {
          "name": "Register Employer",
          "request": {
            "method": "POST",
            "url": "{{base_url}}/api/v1/auth/register-company"
          }
        },
        {
          "name": "Register Candidate",
          "request": {
            "method": "POST",
            "url": "{{base_url}}/api/v1/auth/register-candidate"
          }
        }
      ]
    },
    {
      "name": "2. Jobs",
      "item": []
    },
    {
      "name": "3. Applications",
      "item": []
    },
    {
      "name": "4. Admin",
      "item": []
    }
  ]
}
```

### **🚀 Hướng Dẫn Test API**

#### **Bước 1: Đăng Nhập**
1. Gửi request `POST /api/v1/auth/login` với admin credentials
2. Copy `token` từ response
3. Set biến `jwt_token` trong Postman environment

#### **Bước 2: Test Endpoints**
- Postman sẽ tự động thêm `Authorization: Bearer {{jwt_token}}` header
- Các endpoint yêu cầu auth sẽ hoạt động ngay

#### **Bước 3: Test Pagination**
```
GET /api/v1/jobs?page=0&size=10&keyword=java&sort=createdAt,desc
```

---

## 🌍 Môi Trường & Cấu Hình

### **Profiles Được Hỗ Trợ**

| Profile | Target | Config File | Use Case |
|---------|--------|-------------|----------|
| `dev` | Local JDK | `application-dev.properties` | Development trên máy local |
| `prod` | Docker/VPS | `application-prod.properties` | Production deployment |

### **Environment Variables (Production)**

Khi deploy lên production, set các biến môi trường sau:

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `prod` |
| `DB_HOST` | PostgreSQL hostname | `postgres` hoặc `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `jobrecruitment_db` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `your_secure_password` |
| `JWT_SECRET` | JWT signing key | `(64-char base64 string)` |
| `JWT_EXPIRATION` | Token lifetime (ms) | `86400000` (24h) |

### **Generate JWT Secret**

```bash
# Linux/Mac
openssl rand -base64 64

# Windows (PowerShell)
[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))
```

---

## 🔒 Bảo Mật

### **Best Practices Đã Áp Dụng**

✅ **JWT Authentication**: Stateless, Bearer token-based  
✅ **BCrypt Password Hashing**: Mật khẩu được mã hóa với salt  
✅ **Role-Based Access Control (RBAC)**: @PreAuthorize annotations  
✅ **CORS Configuration**: Chỉ cho phép origins được cấu hình  
✅ **SQL Injection Prevention**: JPA Criteria API & Parameterized Queries  
✅ **XSS Protection**: Spring Security headers  
✅ **CSRF Disabled**: Vì dùng JWT (stateless)  
✅ **Environment Variables**: Secrets không bị commit lên Git  

### **⚠️ Security Checklist (Before Production)**

- [ ] Đổi tất cả mật khẩu mặc định (Admin, Employer, Candidate)
- [ ] Generate JWT secret mạnh (64+ characters)
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (không dùng `update`)
- [ ] Disable Swagger UI trên production (hoặc bảo vệ bằng Basic Auth)
- [ ] Enable HTTPS (SSL/TLS)
- [ ] Set up database backup tự động
- [ ] Enable logging & monitoring (ELK Stack, Grafana)
- [ ] Rate limiting cho public endpoints (Spring Cloud Gateway)

---

## 🤝 Đóng Góp

Chúng tôi hoan nghênh mọi đóng góp! Để đóng góp vào dự án:

1. **Fork** repository này
2. Tạo **feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit** thay đổi của bạn (`git commit -m 'Add some AmazingFeature'`)
4. **Push** lên branch (`git push origin feature/AmazingFeature`)
5. Tạo **Pull Request**

### **Coding Standards**

- Tuân thủ Java Code Conventions
- Sử dụng Lombok để giảm boilerplate code
- Viết JavaDoc cho public methods
- Unit tests cho business logic (JUnit 5)
- RESTful API naming conventions

---

## 📄 Giấy Phép

Dự án này được phân phối dưới giấy phép **MIT License**. Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

```
MIT License

Copyright (c) 2025 Job Recruitment Platform

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 📞 Liên Hệ & Hỗ Trợ

- **GitHub Issues**: [https://github.com/your-username/JobRecruitment/issues](https://github.com/your-username/JobRecruitment/issues)
- **Email**: support@jobrecruitment.com
- **Documentation**: [https://docs.jobrecruitment.com](https://docs.jobrecruitment.com)

---

## 🎯 Roadmap

### **Phase 1** ✅ (Hoàn Thành)
- ✅ JWT Authentication & Authorization
- ✅ User Management (Admin, Employer, Candidate)
- ✅ Job Posting & Management
- ✅ Job Application System
- ✅ CV Management
- ✅ Saved Jobs Feature
- ✅ Admin Dashboard & Moderation

### **Phase 2** 🚧 (Đang Phát Triển)
- 🚧 Email Notifications (Registration, Application Status)
- 🚧 File Upload (Real multipart CV upload)
- 🚧 Search Optimization (Elasticsearch)
- 🚧 Recommendation System (AI-based job matching)

### **Phase 3** 📋 (Lên Kế Hoạch)
- 📋 Real-time Chat (WebSocket)
- 📋 Video Interview Integration
- 📋 Payment Gateway (Premium Job Postings)
- 📋 Mobile App (React Native)

---

## 🙏 Cảm Ơn

Cảm ơn bạn đã quan tâm đến dự án **Job Recruitment Platform**!

Nếu thấy hữu ích, đừng quên ⭐ **Star** repository này nhé! 

**Happy Coding!** 🚀

---

<p align="center">
  Made with ❤️ by <strong>Job Recruitment Team</strong>
</p>
