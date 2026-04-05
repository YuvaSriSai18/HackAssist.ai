# API Testing Guide - Hackathon Assistant Backend

## 🚀 Quick Start

### 1. Server Status
```bash
curl -X GET http://localhost:8080/api/ai/health
```

Response:
```json
{
  "status": "AI Service is running",
  "geminiIntegration": "Ready to process problem statements"
}
```

---

## 👥 User Management APIs

### Register a New User
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "uid": "user001",
    "name": "Alice Johnson",
    "email": "alice@hackathon.com",
    "photoUrl": "https://example.com/alice.jpg"
  }'
```

### Get User by Email
```bash
curl -X GET http://localhost:8080/api/users/email/alice@hackathon.com
```

### Get User by ID
```bash
curl -X GET http://localhost:8080/api/users/user001
```

### List All Users
```bash
curl -X GET http://localhost:8080/api/users
```

### Update User
```bash
curl -X PUT http://localhost:8080/api/users/user001 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice J.",
    "email": "alice.j@hackathon.com",
    "photoUrl": "https://example.com/alice-updated.jpg"
  }'
```

### Delete User
```bash
curl -X DELETE http://localhost:8080/api/users/user001
```

### Check if User Exists
```bash
curl -X GET http://localhost:8080/api/users/exist/alice@hackathon.com
```

---

## 📋 Task Management APIs

### Create a Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Setup Database Schema",
    "description": "Create tables for users and tasks",
    "assignedToUid": "user001",
    "status": "PENDING",
    "priority": "HIGH",
    "estimatedHours": 4.0,
    "dueDate": "2026-04-10T18:00:00"
  }'
```

### Get Task by ID
```bash
curl -X GET http://localhost:8080/api/tasks/1
```

### Get All Tasks
```bash
curl -X GET http://localhost:8080/api/tasks
```

### Get Tasks for a Specific User
```bash
curl -X GET http://localhost:8080/api/tasks/user/user001
```

### Update a Task
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Setup Database Schema - Updated",
    "description": "Create tables for users, tasks, and GitHub commits",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "estimatedHours": 5.0,
    "dueDate": "2026-04-10T18:00:00"
  }'
```

### Start a Task
```bash
curl -X PUT http://localhost:8080/api/tasks/1/start
```

### Complete a Task
```bash
curl -X PUT http://localhost:8080/api/tasks/1/complete
```

### Block a Task
```bash
curl -X PUT http://localhost:8080/api/tasks/1/block?reason=Waiting+for+approval
```

### Get Tasks by Status
```bash
curl -X GET http://localhost:8080/api/tasks/status/IN_PROGRESS
```

### Delete a Task
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

---

## 🤖 AI Integration APIs

### Generate Tasks from Problem Statement
```bash
curl -X POST http://localhost:8080/api/ai/generate-tasks \
  -H "Content-Type: application/json" \
  -d '{
    "problemStatement": "Build a real-time collaborative whiteboard application for remote teams with features like drawing, text annotation, video chat, and export as PDF"
  }'
```

Response Example:
```json
{
  "problemStatement": "Build a real-time collaborative whiteboard...",
  "features": [
    "Real-time drawing sync",
    "Multi-user collaboration",
    "Video chat integration",
    "PDF export"
  ],
  "tasks": [
    {
      "title": "Setup Database Schema",
      "priority": "HIGH",
      "estimatedHours": 4.0,
      "description": "Design and create database tables"
    },
    {
      "title": "Implement WebSocket Server",
      "priority": "HIGH",
      "estimatedHours": 6.0,
      "description": "Setup real-time communication"
    }
  ],
  "message": "Tasks generated successfully"
}
```

---

## 🧪 Sample Workflow

### 1. Register Team Members
```bash
# Register team member 1
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"uid":"dev001","name":"Bob Dev","email":"bob@team.com","photoUrl":"https://..."}'

# Register team member 2
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"uid":"design001","name":"Carol Design","email":"carol@team.com","photoUrl":"https://..."}'
```

### 2. Generate Tasks from Problem Statement
```bash
curl -X POST http://localhost:8080/api/ai/generate-tasks \
  -H "Content-Type: application/json" \
  -d '{"problemStatement":"Your problem statement here"}'
```

### 3. Create and Assign Tasks
```bash
# Create task for backend development
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title":"API Development",
    "description":"Develop REST endpoints",
    "assignedToUid":"dev001",
    "status":"PENDING",
    "priority":"HIGH",
    "estimatedHours":8.0
  }'

# Create task for UI design
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title":"UI Design",
    "description":"Design wireframes and mockups",
    "assignedToUid":"design001",
    "status":"PENDING",
    "priority":"MEDIUM",
    "estimatedHours":6.0
  }'
```

### 4. Track Progress
```bash
# Check all tasks
curl -X GET http://localhost:8080/api/tasks

# Check tasks for specific user
curl -X GET http://localhost:8080/api/tasks/user/dev001

# Check in-progress tasks
curl -X GET http://localhost:8080/api/tasks/status/IN_PROGRESS
```

### 5. Update Task Status
```bash
# Start task
curl -X PUT http://localhost:8080/api/tasks/1/start

# Complete task
curl -X PUT http://localhost:8080/api/tasks/1/complete

# Block task with reason
curl -X PUT http://localhost:8080/api/tasks/1/block?reason=Blocked+by+deployment+server+outage
```

---

## 📊 Database Check

Access H2 Database Console at: `http://localhost:8080/api/h2-console`

**Credentials:**
- Driver: org.h2.Driver
- URL: jdbc:h2:mem:hackathondb
- Username: sa
- Password: (leave blank)

---

## 🔍 HTTP Status Codes

- `200 OK` - Request successful
- `201 Created` - Resource created
- `400 Bad Request` - Invalid input
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## 💡 Tips

1. Use `Content-Type: application/json` for all POST/PUT requests
2. Include all required fields in requests
3. Use ISO 8601 format for timestamps: `YYYY-MM-DDTHH:mm:ss`
4. Task status values: PENDING, IN_PROGRESS, COMPLETED, BLOCKED, CANCELLED
5. Priority values: LOW, MEDIUM, HIGH, CRITICAL

---

**Ready to test!** 🎉
