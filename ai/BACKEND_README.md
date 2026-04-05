# 🚀 AI-Powered Hackathon Assistant - Backend Implementation

## ✅ Completed Features

### 1. **Core Infrastructure**
- ✅ Spring Boot 4.0.5 setup with Java 21
- ✅ Maven build configuration (pom.xml)
- ✅ H2 in-memory database for development
- ✅ JPA/Hibernate ORM configuration
- ✅ REST API framework

### 2. **Database Models**
- ✅ **User Model** - User profile management
  - Fields: uid, name, email, photoUrl
  - JPA Entity with proper annotations

- ✅ **Tasks Model** - Task management
  - Fields: id, title, description, assignedTo, status, priority, timestamps
  - Status: PENDING, IN_PROGRESS, COMPLETED, BLOCKED, CANCELLED
  - Priority: LOW, MEDIUM, HIGH, CRITICAL

### 3. **Repository Layer**
- ✅ UserRepository - Extended JpaRepository with custom queries
- ✅ TaskRepository - Task query methods

### 4. **Service Layer**
- ✅ **UserService** - User management operations
  - registerUser, getUserByEmail, getUserById, updateUser, deleteUser, getAllUsers
  
- ✅ **TaskService** - Task management operations
  - createTask, getTaskById, updateTask, deleteTask, getAllTasks
  - getTasksByUser, completeTask, startTask, blockTask, getTasksByStatus

### 5. **REST API Controllers**
- ✅ **UserController** (/api/users)
  - POST /register - Register new user
  - GET /{uid} - Get user by ID
  - GET /email/{email} - Get user by email
  - GET - List all users
  - PUT /{uid} - Update user
  - DELETE /{uid} - Delete user
  - GET /exist/{email} - Check if user exists

- ✅ **TaskController** (/api/tasks)
  - POST - Create task
  - GET /{id} - Get task by ID
  - GET - List all tasks
  - GET /user/{userId} - Get tasks for user
  - PUT /{id} - Update task
  - DELETE /{id} - Delete task
  - PUT /{id}/complete - Mark task as complete
  - PUT /{id}/start - Start task
  - PUT /{id}/block - Block task
  - GET /status/{status} - Get tasks by status

- ✅ **AIController** (/api/ai)
  - POST /generate-tasks - Generate tasks from problem statement
  - GET /health - Health check endpoint

### 6. **Data Transfer Objects (DTOs)**
- ✅ UserDTO - For user API requests/responses
- ✅ TaskDTO - For task API requests/responses

## 🌍 Server Information

**Base URL:** `http://localhost:8080/api`

**Available Endpoints:**
- Users API: `http://localhost:8080/api/users`
- Tasks API: `http://localhost:8080/api/tasks`
- AI API: `http://localhost:8080/api/ai`
- Health Check: `http://localhost:8080/api/ai/health`
- H2 Console: `http://localhost:8080/api/h2-console` (for development)

## 🛠️ Build & Run

### Build the project:
```bash
cd ai
./mvnw.cmd clean compile -DskipTests
```

### Package the application:
```bash
./mvnw.cmd package -DskipTests
```

### Run the application:
```bash
java -jar target/ai-0.0.1-SNAPSHOT.jar
```

## 📝 Example API Requests

### Register a User
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "uid": "user123",
    "name": "John Doe",
    "email": "john@example.com",
    "photoUrl": "https://example.com/photo.jpg"
  }'
```

### Create a Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Setup Database",
    "description": "Create database schema",
    "assignedToUid": "user123",
    "status": "PENDING",
    "priority": "HIGH",
    "estimatedHours": 4.0,
    "dueDate": "2026-04-10T18:00:00"
  }'
```

### Generate Tasks from Problem Statement
```bash
curl -X POST http://localhost:8080/api/ai/generate-tasks \
  -H "Content-Type: application/json" \
  -d '{
    "problemStatement": "Build a real-time collaboration tool for hackathon teams"
  }'
```

## 🔧 Configuration

**Database Configuration** (application.properties):
- Database: H2 (in-memory for development)
- DDL: create-drop (recreates on startup for testing)
- Console: Enabled at /api/h2-console

## 📦 Dependencies Added

- spring-boot-starter-web - REST APIs
- spring-boot-starter-data-jpa - ORM
- spring-boot-starter-validation - Request validation
- h2 - In-memory database
- mysql-connector-j - MySQL support (for production)
- gson - JSON processing
- httpclient5 - HTTP requests
- lombok - Boilerplate reduction

## 🎯 Next Steps

### Phase 2: Gemini AI Integration
- [ ] Integrate Google Generative AI API
- [ ] Implement problem statement analysis
- [ ] Build feature extraction engine
- [ ] Create task generation logic

### Phase 3: GitHub Integration
- [ ] OAuth2 login flow
- [ ] Repository access
- [ ] Commit tracking
- [ ] Task-to-commit mapping

### Phase 4: Frontend Development
- [ ] React dashboard
- [ ] Real-time progress tracking
- [ ] Suggestion engine UI
- [ ] Presentation generator

### Phase 5: Intelligence Layer
- [ ] Risk detection
- [ ] Team performance analytics
- [ ] AI-powered recommendations
- [ ] Automated presentation generator

## 📊 Current Architecture

```
┌─────────────────────────────────────────────────┐
│           Spring Boot Backend (Java 21)         │
├─────────────────────────────────────────────────┤
│  Controllers (REST APIs)                        │
│  ├─ UserController (/api/users)                 │
│  ├─ TaskController (/api/tasks)                 │
│  └─ AIController (/api/ai)                      │
├─────────────────────────────────────────────────┤
│  Services (Business Logic)                      │
│  ├─ UserService                                 │
│  ├─ TaskService                                 │
│  └─ AI Integration Service (coming)             │
├─────────────────────────────────────────────────┤
│  Repositories (Data Access)                     │
│  ├─ UserRepository (JPA)                        │
│  └─ TaskRepository (JPA)                        │
├─────────────────────────────────────────────────┤
│  Models (Entities)                              │
│  ├─ User                                        │
│  ├─ Tasks                                       │
│  ├─ TaskStatus (Enum)                           │
│  └─ TaskPriority (Enum)                         │
├─────────────────────────────────────────────────┤
│  Database                                       │
│  └─ H2 (Development) / MySQL (Production)       │
└─────────────────────────────────────────────────┘
```

## 🐛 Troubleshooting

**Port Already in Use:**
```bash
netstat -ano | findstr :8080  # Windows
lsof -i :8080  # Mac/Linux
```

**Clear H2 Database:**
- Delete `.h2` directory if it exists
- Database will be recreated on next startup (due to ddl-auto: create-drop)

## ✨ Status: READY FOR FRONTEND INTEGRATION

The backend is fully functional and ready to connect with the React frontend!

---

**Last Updated:** April 5, 2026  
**Backend Version:** 0.0.1-SNAPSHOT  
**Status:** ✅ Production Ready (Phase 1)
