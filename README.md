# School Equipment Lending Service 🎓
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.2-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
A comprehensive RESTful API backend service for managing school equipment lending operations. This system enables educational institutions to efficiently track equipment inventory, handle borrow requests, and manage equipment availability with role-based access control.
## 📋 Table of Contents
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Authentication & Authorization](#-authentication--authorization)
- [Exception Handling](#-exception-handling)
- [Configuration](#-configuration)
- [Development](#-development)
- [Best Practices](#-best-practices)
- [Troubleshooting](#-troubleshooting)
## ✨ Features
### 🔐 Authentication & Authorization
- User registration and JWT-based authentication
- Role-based access control (ADMIN, STAFF, STUDENT)
- Secure password encryption with BCrypt
- Token-based stateless authentication
### 📦 Equipment Management
- Complete CRUD operations for equipment inventory
- Category-based equipment organization
- Real-time availability tracking
- Advanced search and filter capabilities
- Equipment condition monitoring
- Availability status management
### 📝 Borrow Request System
- Submit equipment borrow requests with date ranges
- Multi-step approval/rejection workflow
- Intelligent quantity validation against availability
- Equipment return tracking with condition reporting
- Request status lifecycle (PENDING → APPROVED/REJECTED → RETURNED)
- Automated booking entry creation
- Conflict detection for overlapping bookings
### 🛡️ Security Features
- JWT authentication filter
- Method-level security with @PreAuthorize
- CORS configuration
- SQL injection prevention
- Input validation
### 📊 Logging & Monitoring
- SLF4J with Logback
- Structured logging at multiple levels (INFO, DEBUG, WARN, ERROR)
- Business operation tracking
- Authentication event logging
- Exception logging with context
## 🛠 Tech Stack
### Backend Framework
- **Spring Boot 3.2.5** - Application framework
- **Spring Web** - REST API development
- **Spring Data JPA** - Data persistence
- **Spring Security** - Authentication & authorization
- **Spring Validation** - Request validation
### Database & Migration
- **MySQL 8.2** - Relational database
- **Flyway** - Database version control and migration
- **Hibernate** - ORM framework
### Security
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing
- **JJWT 0.11.5** - JWT implementation
### API Documentation
- **Swagger/OpenAPI 3.0** - Interactive API documentation
- **SpringDoc OpenAPI 2.2.0** - Swagger integration
### Development Tools
- **Lombok** - Boilerplate code reduction
- **Spring DevTools** - Hot reload during development
- **Gradle 8.14** - Build automation
### Containerization
- **Docker** - Container platform
- **Docker Compose** - Multi-container orchestration
## 🏗 Architecture
### Project Structure
```
src/main/java/com/school/equipment/
├── config/                      # Application configuration
│   └── SwaggerConfig.java       # OpenAPI/Swagger configuration
│
├── controller/                  # REST API endpoints (Presentation Layer)
│   ├── AuthController.java      # Authentication endpoints
│   ├── EquipmentController.java # Equipment management endpoints
│   └── BorrowRequestController.java # Borrow request endpoints
│
├── dto/                         # Data Transfer Objects
│   ├── user/                    # User-related DTOs
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── RegisterRequest.java
│   │   └── UserResponse.java
│   ├── equipment/               # Equipment-related DTOs
│   │   ├── EquipmentCreateRequest.java
│   │   ├── EquipmentCreateResponse.java
│   │   ├── EquipmentUpdateRequest.java
│   │   └── EquipmentResponse.java
│   └── borrow/                  # Borrow request DTOs
│       ├── CreateRequest.java
│       ├── CreateResponse.java
│       ├── ApproveRequest.java
│       ├── RejectRequest.java
│       ├── ReturnRequest.java
│       └── BorrowRequestResponse.java
│
├── entity/                      # JPA Entities (Domain Layer)
│   ├── User.java                # User entity
│   ├── Equipment.java           # Equipment entity
│   ├── BorrowRequest.java       # Borrow request entity
│   ├── EquipmentBooking.java    # Daily booking entries
│   ├── Role.java                # User role enum
│   └── Status.java              # Request status enum
│
├── repository/                  # Data Access Layer
│   ├── UserRepository.java
│   ├── EquipmentRepository.java
│   ├── BorrowRequestRepository.java
│   └── EquipmentBookingRepository.java
│
├── service/                     # Business Logic Layer
│   ├── AuthService.java         # Authentication business logic
│   ├── EquipmentService.java    # Equipment business logic
│   └── BorrowRequestService.java # Borrow request business logic
│
├── security/                    # Security Configuration
│   ├── SecurityConfig.java      # Spring Security configuration
│   ├── JwtUtil.java             # JWT utility methods
│   ├── JwtAuthenticationFilter.java # JWT request filter
│   └── AuthenticationHelper.java # Authentication utilities
│
├── exception/                   # Custom Exception Handling
│   ├── GlobalExceptionHandler.java # @RestControllerAdvice
│   ├── ResourceNotFoundException.java
│   ├── EquipmentNotAvailableException.java
│   ├── InvalidRequestException.java
│   ├── InvalidOperationException.java
│   ├── UserAlreadyExistsException.java
│   ├── EmailAlreadyExistsException.java
│   ├── InvalidCredentialsException.java
│   ├── ErrorResponse.java
│   └── ValidationErrorResponse.java
│
└── EquipmentLendingApplication.java # Main application class
src/main/resources/
├── application.properties       # Application configuration
└── db.migration/                # Flyway migration scripts
    ├── V1__create_users_table.sql
    ├── V2__create_equipment_table.sql
    ├── V3__add_availability_to_equipment.sql
    └── V4__create_borrow_request_and_equipment_booking_tables.sql
```
### Architecture Layers
```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                     │
│  Controllers (REST API Endpoints + Swagger Annotations)   │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                     Business Layer                        │
│     Services (Business Logic + Transaction Management)    │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                   Data Access Layer                       │
│        Repositories (JPA/Hibernate Operations)            │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                      Database Layer                       │
│                      MySQL 8.2                            │
└─────────────────────────────────────────────────────────┘
Cross-Cutting Concerns:
├── Security (JWT Authentication Filter)
├── Exception Handling (@RestControllerAdvice)
├── Logging (SLF4J)
└── Validation (Jakarta Validation)
```
## 🚀 Getting Started
### Prerequisites
- **Java 17** or higher ([Download](https://adoptium.net/))
- **Docker** and **Docker Compose** ([Download](https://www.docker.com/products/docker-desktop))
- **Gradle 8.14+** (wrapper included)
- **Git** ([Download](https://git-scm.com/))
### Installation
#### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/school-equipment-lending-service.git
cd school-equipment-lending-service
```
#### 2. Start MySQL Database with Docker
```bash
docker-compose up -d
```
This will start MySQL 8.2 on port **3307** with:
- Root password: `admin`
- Container name: `mysql`
#### 3. Create Database and User
```bash
# Connect to MySQL
docker exec -it mysql mysql -uroot -padmin
```
```sql
-- Create database
CREATE DATABASE equipment_lending;
-- Create application user
CREATE USER 'school_admin'@'%' IDENTIFIED BY 'adminPassword123!';
-- Grant privileges
GRANT ALL PRIVILEGES ON equipment_lending.* TO 'school_admin'@'%';
FLUSH PRIVILEGES;
-- Verify
SHOW DATABASES;
EXIT;
```
#### 4. Build the Application
```bash
# Using Gradle wrapper
./gradlew clean build
# Or build without tests
./gradlew clean build -x test
```
#### 5. Run the Application
```bash
# Using Gradle
./gradlew bootRun
# Or using Java directly
java -jar build/libs/equipment-lending-1.0-SNAPSHOT.jar
```
The application will start on `http://localhost:8080`
#### 6. Verify Installation
Open your browser and navigate to:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
### Quick Start with Sample Data
#### Step 1: Register an Admin User
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "fullName": "System Administrator",
    "email": "admin@school.com",
    "role": "ADMIN"
  }'
```
**Response:**
```json
{
  "userId": 1,
  "username": "admin",
  "fullName": "System Administrator",
  "email": "admin@school.com",
  "role": "ADMIN"
}
```
#### Step 2: Login to Get JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "ADMIN",
  "fullName": "System Administrator",
  "userId": 1
}
```
#### Step 3: Create Equipment (Using Token)
```bash
curl -X POST http://localhost:8080/api/equipment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Dell Laptop",
    "category": "Electronics",
    "conditionStatus": "Good",
    "totalQuantity": 10,
    "availability": true,
    "description": "Dell Inspiron 15 with 8GB RAM"
  }'
```
#### Step 4: Create a Borrow Request (As Student)
```bash
# First, register a student user
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "student123",
    "fullName": "John Doe",
    "email": "john@school.com",
    "role": "STUDENT"
  }'
# Login as student and get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "john_doe", "password": "student123"}'
# Create borrow request
curl -X POST http://localhost:8080/api/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer STUDENT_JWT_TOKEN" \
  -d '{
    "equipmentId": 1,
    "quantity": 2,
    "fromDate": "2024-12-01",
    "toDate": "2024-12-05",
    "reason": "For project work"
  }'
```
## 📚 API Documentation
### Interactive Documentation
Once the application is running, access the **Swagger UI** for interactive API testing:
```
http://localhost:8080/swagger-ui.html
```
### API Endpoints Summary
#### 🔐 Authentication Endpoints (`/api/auth`)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/signup` | Register new user | Public |
| POST | `/api/auth/login` | Login and get JWT token | Public |
#### 📦 Equipment Endpoints (`/api/equipment`)
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/equipment` | List all equipment with filters | Authenticated |
| GET | `/api/equipment/{id}` | Get equipment by ID | Authenticated |
| POST | `/api/equipment` | Create new equipment | ADMIN |
| PUT | `/api/equipment/{id}` | Update equipment | ADMIN |
| DELETE | `/api/equipment/{id}` | Delete equipment | ADMIN |
**Query Parameters for GET /api/equipment:**
- `category` (optional): Filter by category
- `availableOnly` (optional): Show only available equipment
- `search` (optional): Search in name/description
#### 📝 Borrow Request Endpoints (`/api/requests`)
| Method | Endpoint | Description | Access          |
|--------|----------|-------------|-----------------|
| POST | `/api/requests` | Create borrow request | STUDENT         |
| GET | `/api/requests` | List all requests (with filters) | ADMIN, STAFF    |
| GET | `/api/requests/{id}` | Get request by ID | Authenticated   |
| GET | `/api/requests/my` | Get my requests | Authenticated   |
| GET | `/api/requests/pending` | Get pending requests | ADMIN, STAFF    |
| PUT | `/api/requests/{id}/approve` | Approve request | ADMIN, STAFF    |
| PUT | `/api/requests/{id}/reject` | Reject request | ADMIN, STAFF    |
| PUT | `/api/requests/{id}/return` | Mark as returned | ADMIN, STAFF    |
**Query Parameters for GET /api/requests:**
- `status` (optional): Filter by status (PENDING, APPROVED, REJECTED, RETURNED)
- `userId` (optional): Filter by user ID
## 🗄 Database Schema
### Entity Relationship Diagram
```
┌─────────────────────┐
│       USERS         │
├─────────────────────┤
│ PK  user_id         │
│     username        │
│     password_hash   │
│     full_name       │
│     email           │
│     role            │ (ADMIN, STAFF, STUDENT)
│     created_at      │
│     updated_at      │
└──────────┬──────────┘
           │
           │ 1:N (created_by)
           ├─────────────────────┐
           │                     │
           │ 1:N                 │ 1:N
           │ (requested_by)      │ (approved_by)
           ↓                     ↓
┌──────────────────────┐  ┌───────────────────────┐
│     EQUIPMENT        │  │   BORROW_REQUEST      │
├──────────────────────┤  ├───────────────────────┤
│ PK  equipment_id     │←─│ FK  equipment_id      │
│     name             │1:N│ FK  requested_by      │
│     category         │  │ FK  approved_by       │
│     condition_status │  │     quantity          │
│     total_quantity   │  │     from_date         │
│     available_qty    │  │     to_date           │
│     availability     │  │     return_date       │
│     description      │  │     reason            │
│ FK  created_by       │  │     status            │
│     created_at       │  │     remarks           │
│     updated_at       │  │     condition_after   │
└──────────┬───────────┘  │     created_at        │
           │              │     updated_at        │
           │              └──────────┬────────────┘
           │                         │
           │ 1:N                     │ 1:N
           │                         │
           └─────────┬───────────────┘
                     ↓
          ┌──────────────────────┐
          │  EQUIPMENT_BOOKING   │
          ├──────────────────────┤
          │ PK  booking_id       │
          │ FK  request_id       │
          │ FK  equipment_id     │
          │     booking_date     │
          │     quantity         │
          │     status           │ (ACTIVE, RELEASED)
          │     created_at       │
          │     updated_at       │
          └──────────────────────┘
```
### Table Descriptions
#### **users**
Stores user account information and credentials.
```sql
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);
```
#### **equipment**
Stores equipment inventory information.
```sql
CREATE TABLE equipment (
    equipment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    condition_status VARCHAR(50) DEFAULT 'Good',
    total_quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    availability BOOLEAN DEFAULT true,
    description TEXT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);
```
#### **borrow_request**
Stores equipment borrow requests.
```sql
CREATE TABLE borrow_request (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    requested_by BIGINT NOT NULL,
    approved_by BIGINT,
    quantity INT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    return_date DATE,
    reason VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING',
    remarks VARCHAR(255),
    condition_after_use VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id) ON DELETE CASCADE,
    FOREIGN KEY (requested_by) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(user_id) ON DELETE SET NULL
);
```
#### **equipment_booking**
Stores daily booking entries for equipment (created when request is approved).
```sql
CREATE TABLE equipment_booking (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES borrow_request(request_id) ON DELETE CASCADE,
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id) ON DELETE CASCADE
);
```
### Relationships
| Relationship | Cardinality | Description |
|--------------|-------------|-------------|
| User → Equipment | 1:N | One user can create many equipment items |
| User → BorrowRequest (requested) | 1:N | One user can make many borrow requests |
| User → BorrowRequest (approved) | 1:N | One user can approve many requests |
| Equipment → BorrowRequest | 1:N | One equipment can be in many requests |
| BorrowRequest → EquipmentBooking | 1:N | One request creates multiple daily bookings |
| Equipment → EquipmentBooking | 1:N | One equipment can have many bookings |
## 🔐 Authentication & Authorization
### JWT Token Structure
The application uses JWT tokens for stateless authentication:
```json
{
  "sub": "admin",           // Username
  "role": "ADMIN",          // User role (without ROLE_ prefix)
  "userId": 1,              // User ID for quick access
  "iat": 1701234567,        // Issued at timestamp
  "exp": 1701320967         // Expiration timestamp (24 hours)
}
```
### Token Usage
Include the JWT token in the Authorization header for protected endpoints:
```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsInVzZXJJZCI6MSwiaWF0IjoxNzAxMjM0NTY3LCJleHAiOjE3MDEzMjA5Njd9.xyz...
```
### Role-Based Access Control
| Role | Description | Permissions |
|------|-------------|-------------|
| **ADMIN** | System administrator | Full access - manage equipment, approve/reject/return requests, view all data |
| **LAB_ASSISTANT** | Laboratory staff | Manage requests - approve/reject/return, view equipment |
| **TEACHER** | Faculty member | Create borrow requests, view equipment |
| **STUDENT** | Student | Create borrow requests, view own requests, view equipment |
### Endpoint Access Matrix
| Endpoint | ADMIN |  STAFF | STUDENT |
|----------|-------|--------|-|
| POST /api/equipment | ✅ | ✅ | ❌ |
| PUT/DELETE /api/equipment/{id} | ✅ | ✅ | ❌ |
| GET /api/equipment | ✅ | ✅ | ✅ |
| POST /api/requests | ❌ | ❌ | ✅ |
| GET /api/requests | ✅ | ✅ | ❌|
| GET /api/requests/my | ✅ | ✅ | ✅ |
| GET /api/requests/pending | ✅ | ✅ | ❌ |
| PUT /api/requests/{id}/approve | ✅ | ✅ | ❌ |
| PUT /api/requests/{id}/reject | ✅ | ✅ | ❌ |
| PUT /api/requests/{id}/return | ✅ | ✅ | ❌ |
## ⚠️ Exception Handling
### Custom Exceptions
The application uses custom exceptions for better error handling:
| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `ResourceNotFoundException` | 404 | Entity not found (User, Equipment, Request) |
| `EquipmentNotAvailableException` | 409 | Equipment not available for requested period |
| `InvalidRequestException` | 400 | Invalid request data (dates, quantities) |
| `InvalidOperationException` | 400 | Invalid state transition (e.g., approve non-pending request) |
| `UserAlreadyExistsException` | 400 | Username already exists |
| `EmailAlreadyExistsException` | 400 | Email already exists |
| `InvalidCredentialsException` | 401 | Invalid login credentials |
### Error Response Format
```json
{
  "status": 404,
  "message": "Equipment not found with id: 999"
}
```
### Validation Error Response
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "username": "Username is required",
    "password": "Password must be at least 6 characters"
  }
}
```
## ⚙️ Configuration
### Application Properties
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3307/equipment_lending
spring.datasource.username=school_admin
spring.datasource.password=adminPassword123!
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
# Flyway Configuration
spring.flyway.enabled=true
# JWT Configuration
jwt.expiration=86400000  # 24 hours in milliseconds
# Logging Configuration
logging.level.org.springframework.security=DEBUG
logging.level.com.school.equipment=INFO
# Server Configuration
server.port=8080
```
### Environment Variables
You can override configuration using environment variables:
```bash
# Database
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/equipment_lending
export SPRING_DATASOURCE_USERNAME=school_admin
export SPRING_DATASOURCE_PASSWORD=adminPassword123!
# JWT
export JWT_EXPIRATION=86400000
# Run application
java -jar build/libs/equipment-lending-1.0-SNAPSHOT.jar
```
### Docker Compose Configuration
```yaml
version: '3.9'
volumes:
  mysql_data: {}
services:
  mysql:
    image: mysql:8.2
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: admin
    volumes:
      - mysql_data:/var/lib/mysql:Z
    ports:
      - "3307:3306"
    command: --default-authentication-plugin=mysql_native_password
```
## 💻 Development
### Running Tests
```bash
# Run all tests
./gradlew test
# Run tests with coverage
./gradlew test jacocoTestReport
# Run specific test class
./gradlew test --tests "AuthServiceTest"
```
### Building for Production
```bash
# Build JAR file
./gradlew clean build -x test
# Run the JAR
java -jar build/libs/equipment-lending-1.0-SNAPSHOT.jar
# Build with specific profile
./gradlew clean build -Pprofile=prod
```
### Hot Reload with Spring DevTools
Spring DevTools is included for automatic restart during development:
```bash
# Run with DevTools
./gradlew bootRun
# Make changes to code - application will auto-restart
```
### Accessing H2 Console (for development)
If using H2 database for development:
```properties
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
```
Access at: http://localhost:8080/h2-console
## 🎯 Best Practices Implemented
### ✅ Code Quality
1. **Constructor Injection** - All dependencies use constructor injection (immutable)
2. **Lombok Integration** - Reduces boilerplate code with @Slf4j, @RequiredArgsConstructor
3. **Custom Exceptions** - Specific exceptions instead of generic RuntimeException
4. **DTOs Pattern** - Separate DTOs for requests/responses
5. **Service Layer** - Business logic separated from controllers
6. **Repository Pattern** - JPA repositories for data access
### ✅ Security
1. **JWT Authentication** - Stateless token-based auth
2. **Password Encryption** - BCrypt hashing
3. **Method Security** - @PreAuthorize annotations
4. **CORS Configuration** - Controlled cross-origin access
5. **Input Validation** - Jakarta Validation on DTOs
### ✅ API Design
1. **RESTful Principles** - Proper HTTP methods and status codes
2. **Swagger Documentation** - Complete OpenAPI annotations
3. **Consistent Response Format** - Standardized DTOs
4. **Error Handling** - Global exception handler
5. **Versioning Ready** - Structured for future versioning
### ✅ Database
1. **Flyway Migrations** - Version-controlled schema changes
2. **Proper Indexing** - Indexed foreign keys and search columns
3. **Cascading Rules** - Appropriate cascade operations
4. **Constraints** - Database-level constraints
### ✅ Logging
1. **Structured Logging** - SLF4J with meaningful context
2. **Appropriate Levels** - INFO, DEBUG, WARN, ERROR
3. **Business Events** - Track important operations
4. **Security Events** - Log authentication attempts
## 🐛 Troubleshooting
### Common Issues
#### 1. Database Connection Failed
**Problem**: `Cannot create PoolableConnectionFactory`
**Solutions**:
```bash
# Check if MySQL is running
docker ps | grep mysql
# Check MySQL logs
docker logs mysql
# Restart MySQL
docker-compose down
docker-compose up -d
# Verify database exists
docker exec -it mysql mysql -uschool_admin -padminPassword123! -e "SHOW DATABASES;"
```
#### 2. Access Denied / 403 Forbidden
**Problem**: Getting 403 when calling authenticated endpoints
**Solutions**:
1. **Verify token is valid**:
    - Check token hasn't expired (24 hours)
    - Decode token at https://jwt.io
2. **Check Authorization header**:
   ```bash
   # Correct format
   Authorization: Bearer eyJhbGci...
   ```
3. **Verify user role**:
    - Check database: `SELECT username, role FROM users;`
    - Ensure role matches endpoint requirements
4. **Check logs**:
    - Look for JWT authentication logs
    - Verify role is being extracted correctly
#### 3. Flyway Migration Errors
**Problem**: `FlywayException: Found non-empty schema without metadata table`
**Solutions**:
```bash
# Option 1: Clean Flyway history
docker exec -it mysql mysql -uschool_admin -padminPassword123! equipment_lending
DELETE FROM flyway_schema_history WHERE success = 0;
# Option 2: Baseline existing database
spring.flyway.baseline-on-migrate=true
# Option 3: Drop and recreate database
DROP DATABASE equipment_lending;
CREATE DATABASE equipment_lending;
```
#### 4. Port Already in Use
**Problem**: `Port 8080 is already in use`
**Solutions**:
```bash
# Find process using port 8080
lsof -i :8080
# Kill the process
kill -9 <PID>
# Or use different port
server.port=8081
```
#### 5. JWT Token Invalid or Expired
**Problem**: Token validation fails
**Solutions**:
1. **Token expired** - Login again to get new token
2. **Wrong secret key** - Server restart changes key (not production-ready)
3. **Malformed token** - Check token format at jwt.io
```bash
# Get new token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```
#### 6. Equipment Not Available Error
**Problem**: "Equipment not available for the requested period"
**Causes**:
- Overlapping bookings
- Insufficient quantity
- Incorrect date range
  **Debug**:
```sql
-- Check current bookings
SELECT * FROM equipment_booking 
WHERE equipment_id = ? 
  AND booking_date BETWEEN ? AND ?
  AND status = 'ACTIVE';
-- Check equipment quantities
SELECT equipment_id, total_quantity, available_quantity 
FROM equipment WHERE equipment_id = ?;
```
### Debug Mode
Enable detailed logging for troubleshooting:
```properties
# Trace level logging
logging.level.org.springframework.security=TRACE
logging.level.com.school.equipment=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
# Show all SQL queries
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```
### Health Check Endpoints
Add Spring Actuator for health monitoring:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```
Access:
- Health: http://localhost:8080/actuator/health
- Info: http://localhost:8080/actuator/info
## 📄 License
This project is licensed under the MIT License.
## 🤝 Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
### Code Style Guidelines
- Use **constructor injection** for dependencies
- Add **Lombok annotations** (@Slf4j, @RequiredArgsConstructor)
- Use **custom exceptions** instead of generic RuntimeException
- Add **Swagger documentation** to all endpoints
- Write **meaningful log messages**
- Follow **RESTful conventions**
- Add **unit tests** for new features
## 📧 Support
For issues and questions:
- **GitHub Issues**: https://github.com/yourusername/school-equipment-lending-service/issues
- **Documentation**: See Swagger UI at `/swagger-ui.html`
## 🙏 Acknowledgments
- Spring Boot Team for the excellent framework
- MySQL for the robust database system
- JWT.io for token debugging tools
- Swagger/OpenAPI for API documentation
---

**Last Updated**: November 2025

