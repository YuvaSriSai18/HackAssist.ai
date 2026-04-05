## AI-Powered Hackathon Assistant - Comprehensive API Documentation

### Project Overview
This API serves a full-stack AI-powered hackathon assistant system with modules for:
- Task generation from problem statements (Gemini AI)
- GitHub repository integration and commit tracking
- Progress monitoring and risk detection
- Team activity intelligence

---

## API ENDPOINTS & SAMPLE RESPONSES

### 1. USER MANAGEMENT APIs

#### POST /api/users/register
**Description:** Register a new user

**Request Body:**
```json
{
  "uid": "user123",
  "name": "John Doe",
  "email": "john@example.com",
  "photoUrl": "https://example.com/photo.jpg"
}
```

**Response (201 Created):**
```json
{
  "uid": "user123",
  "name": "John Doe",
  "email": "john@example.com",
  "photoUrl": "https://example.com/photo.jpg"
}
```

#### GET /api/users/{uid}
**Description:** Get user details

**Response (200 OK):**
```json
{
  "uid": "user123",
  "name": "John Doe",
  "email": "john@example.com",
  "photoUrl": "https://example.com/photo.jpg"
}
```

---

### 2. PROJECT MANAGEMENT APIs

#### POST /api/projects
**Description:** Create a new hackathon project

**Request Body:**
```json
{
  "name": "AI-Powered E-Commerce Platform",
  "description": "An intelligent shopping platform with recommendation engine",
  "problemStatement": "Create an e-commerce platform that uses AI to recommend products based on user behavior",
  "createdByUid": "user123",
  "hackathonTheme": "AI & Machine Learning"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "AI-Powered E-Commerce Platform",
  "description": "An intelligent shopping platform with recommendation engine",
  "problemStatement": "Create an e-commerce platform that uses AI to recommend products...",
  "createdBy": {
    "uid": "user123",
    "name": "John Doe"
  },
  "createdAt": "2026-04-05T17:25:00",
  "updatedAt": "2026-04-05T17:25:00",
  "status": "ACTIVE",
  "githubRepoUrl": null,
  "aiGeneratedFeatures": null
}
```

#### GET /api/projects
**Description:** Get all projects

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "AI-Powered E-Commerce Platform",
    "description": "An intelligent shopping platform with recommendation engine",
    "status": "ACTIVE",
    "createdAt": "2026-04-05T17:25:00"
  },
  {
    "id": 2,
    "name": "Real-time Chat Application",
    "description": "Chat app with real-time notifications",
    "status": "ACTIVE",
    "createdAt": "2026-04-05T16:45:00"
  }
]
```

#### GET /api/projects/{id}
**Description:** Get project details

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "AI-Powered E-Commerce Platform",
  "description": "...",
  "problemStatement": "...",
  "createdBy": {...},
  "status": "ACTIVE",
  "createdAt": "2026-04-05T17:25:00",
  "updatedAt": "2026-04-05T17:25:00"
}
```

#### PUT /api/projects/{id}
**Description:** Update project

**Request Body:**
```json
{
  "name": "Updated Project Name",
  "description": "Updated description",
  "problemStatement": "Updated problem statement",
  "githubRepoUrl": "https://github.com/user/repo"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Updated Project Name",
  "description": "Updated description",
  "problemStatement": "Updated problem statement",
  "githubRepoUrl": "https://github.com/user/repo",
  "updatedAt": "2026-04-05T17:30:00"
}
```

#### DELETE /api/projects/{id}
**Description:** Delete project

**Response (200 OK):**
```json
{
  "message": "Project deleted successfully"
}
```

#### GET /api/projects/user/{uid}
**Description:** Get all projects created by a user

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "AI-Powered E-Commerce Platform",
    "status": "ACTIVE",
    "createdAt": "2026-04-05T17:25:00"
  }
]
```

---

### 3. AI TASK GENERATION APIs

#### POST /api/ai/generate-tasks
**Description:** Generate tasks from problem statement using Gemini AI

**Request Body:**
```json
{
  "projectId": 1,
  "problemStatement": "Create an AI-powered chatbot that can handle customer support queries with context awareness",
  "hackathonTheme": "AI & Natural Language Processing"
}
```

**Response (200 OK):**
```json
{
  "projectName": "AI-Powered E-Commerce Platform",
  "projectSummary": "Build an intelligent customer support chatbot with NLP capabilities. The system should understand customer queries, provide contextual responses, and escalate complex issues to human agents.",
  "features": [
    {
      "name": "Natural Language Understanding",
      "description": "Process customer queries and extract intent",
      "priority": "CRITICAL",
      "technologiesNeeded": ["BERT", "TensorFlow", "Python"]
    },
    {
      "name": "Context Management",
      "description": "Maintain conversation history and context",
      "priority": "HIGH",
      "technologiesNeeded": ["Redis", "PostgreSQL"]
    },
    {
      "name": "Response Generation",
      "description": "Generate relevant responses using fine-tuned models",
      "priority": "HIGH",
      "technologiesNeeded": ["GPT", "LangChain"]
    }
  ],
  "tasks": [
    {
      "title": "Setup NLP Pipeline",
      "description": "Configure BERT model and preprocessing pipeline",
      "priority": "CRITICAL",
      "estimatedHours": 6,
      "category": "Backend",
      "suggestedAssignee": "user456"
    },
    {
      "title": "Build REST API for Chatbot",
      "description": "Create GET/POST endpoints for chat interactions",
      "priority": "HIGH",
      "estimatedHours": 4,
      "category": "Backend",
      "suggestedAssignee": "user123"
    },
    {
      "title": "Create Frontend Interface",
      "description": "Build React component for chat UI",
      "priority": "HIGH",
      "estimatedHours": 5,
      "category": "Frontend",
      "suggestedAssignee": "user789"
    }
  ],
  "recommendedTechnologies": [
    "Python 3.9+",
    "Spring Boot / FastAPI",
    "React.js",
    "PostgreSQL",
    "Docker",
    "Kubernetes"
  ]
}
```

#### POST /api/ai/generate-features
**Description:** Generate features from problem statement

**Request Body:**
```json
{
  "problemStatement": "Build a real-time collaborative document editor"
}
```

**Response (200 OK):**
```json
[
  {
    "name": "Real-time Synchronization",
    "description": "Sync changes across all connected users in real-time",
    "priority": "CRITICAL",
    "technologiesNeeded": ["WebSocket", "Op-Transform", "Redis"]
  },
  {
    "name": "Conflict Resolution",
    "description": "Handle concurrent edits from multiple users",
    "priority": "HIGH",
    "technologiesNeeded": ["CRDTs", "Operational Transformation"]
  },
  {
    "name": "Version Control",
    "description": "Track document changes and enable rollback",
    "priority": "MEDIUM",
    "technologiesNeeded": ["Git", "TimeSeries DB"]
  }
]
```

#### GET /api/ai/summary/{projectId}
**Description:** Generate AI summary for a project

**Response (200 OK):**
```json
{
  "summary": "Project generated from problem statement. Implementation focuses on building a scalable, real-time chatbot with advanced NLP capabilities. Core technologies: BERT, FastAPI, React, PostgreSQL. Priority on natural language understanding and context management."
}
```

#### GET /api/ai/health
**Description:** Check AI service status

**Response (200 OK):**
```json
{
  "status": "AI Service is running",
  "geminiIntegration": "Ready to process problem statements"
}
```

---

### 4. TASK MANAGEMENT APIs

#### POST /api/tasks
**Description:** Create a task

**Request Body:**
```json
{
  "title": "Setup Database Schema",
  "description": "Design and create database tables for users, tasks, and projects",
  "assignedToUid": "user123",
  "status": "PENDING",
  "priority": "HIGH",
  "dueDate": "2026-04-10T23:59:00",
  "estimatedHours": 4.0
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "Setup Database Schema",
  "description": "Design and create database tables for users, tasks, and projects",
  "assignedTo": {
    "uid": "user123",
    "name": "John Doe"
  },
  "status": "PENDING",
  "priority": "HIGH",
  "createdAt": "2026-04-05T17:25:00",
  "updatedAt": "2026-04-05T17:25:00",
  "dueDate": "2026-04-10T23:59:00",
  "estimatedHours": 4.0,
  "completedAt": null
}
```

#### GET /api/tasks
**Description:** Get all tasks

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Setup Database Schema",
    "status": "PENDING",
    "priority": "HIGH",
    "assignedTo": {"uid": "user123", "name": "John Doe"}
  }
]
```

#### GET /api/tasks/{id}
**Description:** Get task details

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Setup Database Schema",
  "description": "...",
  "status": "PENDING",
  "priority": "HIGH",
  "assignedTo": {...}
}
```

#### PUT /api/tasks/{id}
**Description:** Update task

**Request Body:**
```json
{
  "title": "Setup Database Schema",
  "status": "IN_PROGRESS",
  "estimatedHours": 5.0
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Setup Database Schema",
  "status": "IN_PROGRESS",
  "estimatedHours": 5.0,
  "updatedAt": "2026-04-05T18:00:00"
}
```

#### DELETE /api/tasks/{id}
**Description:** Delete task

**Response (200 OK):**
```json
{
  "message": "Task deleted successfully"
}
```

---

### 5. GITHUB INTEGRATION APIs

#### GET /api/github/repos?userToken={token}
**Description:** Fetch user's GitHub repositories

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "repoName": "hackathon-project",
    "repoUrl": "https://github.com/user/hackathon-project",
    "owner": "user",
    "description": "AI-powered hackathon platform",
    "stars": 42,
    "forks": 15,
    "openIssues": 3,
    "createdAt": "2026-03-15T10:00:00",
    "lastSynced": "2026-04-05T17:20:00"
  }
]
```

#### GET /api/github/commits/{repositoryId}
**Description:** Fetch commits from a GitHub repository

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "commitHash": "abc123def456",
    "author": "john@example.com",
    "message": "Implement user authentication",
    "commitDate": "2026-04-05T15:30:00",
    "repositoryId": 1,
    "mappedTaskId": 5,
    "createdAt": "2026-04-05T15:35:00",
    "additions": 150,
    "deletions": 25,
    "fileChanges": 8
  },
  {
    "id": 2,
    "commitHash": "def789ghi012",
    "author": "jane@example.com",
    "message": "Add task creation API",
    "commitDate": "2026-04-05T16:00:00",
    "repositoryId": 1,
    "mappedTaskId": 6,
    "additions": 200,
    "deletions": 50,
    "fileChanges": 12
  }
]
```

#### POST /api/github/map-commits
**Description:** Map commits to tasks using keyword matching

**Request Body:**
```json
{
  "projectId": 1
}
```

**Response (200 OK):**
```json
{
  "message": "Commits mapped to tasks successfully"
}
```

#### GET /api/github/repository/{repositoryId}/commits
**Description:** Get commits for a specific repository

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "commitHash": "abc123def456",
    "author": "john@example.com",
    "message": "Implement user authentication",
    "commitDate": "2026-04-05T15:30:00",
    "fileChanges": 8
  }
]
```

---

### 6. PROGRESS TRACKING APIs

#### GET /api/progress/project/{projectId}
**Description:** Get comprehensive project progress

**Response (200 OK):**
```json
{
  "projectId": 1,
  "taskCompletionPercentage": 65.5,
  "commitContributionPercentage": 72.3,
  "totalTasks": 20,
  "completedTasks": 13,
  "totalCommits": 45,
  "totalTeamMembers": 5,
  "overallHealthStatus": "GOOD"
}
```

#### GET /api/progress/task-completion/{projectId}
**Description:** Get task completion percentage

**Response (200 OK):**
```json
{
  "taskCompletionPercentage": 65.5
}
```

#### GET /api/progress/commit-contribution/{projectId}
**Description:** Get commit contribution percentage

**Response (200 OK):**
```json
{
  "commitContributionPercentage": 72.3
}
```

---

### 7. INTELLIGENCE & RISK DETECTION APIs

#### POST /api/intelligence/detect-risks/{projectId}
**Description:** Detect risks in project

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "projectId": 1,
    "riskType": "NO_COMMITS_IN_HOURS",
    "description": "No commits detected in the last 6 hours",
    "severity": "MEDIUM",
    "detectedAt": "2026-04-05T17:30:00",
    "resolvedAt": null,
    "status": "ACTIVE",
    "suggestedAction": "Check team member status and encourage code commits"
  },
  {
    "id": 2,
    "projectId": 1,
    "riskType": "TOO_MANY_PENDING_TASKS",
    "description": "Too many pending tasks: 15",
    "severity": "HIGH",
    "detectedAt": "2026-04-05T17:35:00",
    "resolvedAt": null,
    "status": "ACTIVE",
    "suggestedAction": "Prioritize key tasks and assign them to team members"
  }
]
```

#### GET /api/intelligence/summary/{projectId}
**Description:** Get complete project summary for presentation

**Response (200 OK):**
```json
{
  "projectId": 1,
  "projectName": "AI-Powered E-Commerce Platform",
  "description": "An intelligent shopping platform with recommendation engine",
  "status": "ACTIVE",
  "completionPercentage": 65.5,
  "teamActivity": [
    {
      "memberUid": "user123",
      "memberName": "John Doe",
      "assignedTasks": 8,
      "completedTasks": 6,
      "totalCommits": 15,
      "lastActivityTime": "2026-04-05T16:45:00",
      "activityStatus": "ACTIVE"
    },
    {
      "memberUid": "user789",
      "memberName": "Jane Smith",
      "assignedTasks": 7,
      "completedTasks": 5,
      "totalCommits": 12,
      "lastActivityTime": "2026-04-05T17:10:00",
      "activityStatus": "ACTIVE"
    }
  ],
  "activeRisks": [
    {
      "riskType": "TOO_MANY_PENDING_TASKS",
      "severity": "HIGH",
      "description": "Too many pending tasks: 15"
    }
  ],
  "achievedFeatures": [
    "User Authentication",
    "Task Management API",
    "Real-time Progress Tracking"
  ],
  "pendingFeatures": [
    "AI Recommendation Engine",
    "Advanced Analytics Dashboard",
    "Mobile App"
  ]
}
```

#### POST /api/intelligence/suggest-actions/{projectId}
**Description:** Get AI-generated suggestions for next actions

**Response (200 OK):**
```json
{
  "suggestions": "• Prioritize remaining high-priority tasks\n• Begin testing and quality assurance\n• Assign pending tasks to available team members\n• Create rollout plan for completed features\n• Schedule team sync meetings"
}
```

---

## ARCHITECTURE OVERVIEW

```
Client (React.js Frontend)
        ↓
    API Gateway (/api)
        ↓
┌───────────────────────────────────────┐
│     Spring Boot REST Controllers      │
├───────────────────────────────────────┤
│  ├─ ProjectController                 │
│  ├─ AIController (with Gemini)        │
│  ├─ GitHubController                  │
│  ├─ ProgressController                │
│  ├─ TaskController                    │
│  ├─ UserController                    │
│  └─ IntelligenceController            │
└───────────────────────────────────────┘
        ↓
┌───────────────────────────────────────┐
│     Service Layer (Business Logic)    │
├───────────────────────────────────────┤
│  ├─ ProjectService (IProjectService)  │
│  ├─ GeminiService (IAIService)        │
│  ├─ GitHubService (IGitHubService)    │
│  ├─ ProgressService (IProgressService)│
│  ├─ TaskService                       │
│  ├─ UserService                       │
│  └─ IntelligenceService               │
└───────────────────────────────────────┘
        ↓
┌───────────────────────────────────────┐
│     Repository Layer (Data Access)    │
├───────────────────────────────────────┤
│  ├─ ProjectRepository                 │
│  ├─ GitHubRepositoryRepository        │
│  ├─ GitCommitRepository               │
│  ├─ RiskAlertRepository               │
│  ├─ TaskRepository                    │
│  ├─ UserRepository                    │
│  └─ JPA/Hibernate ORM                 │
└───────────────────────────────────────┘
        ↓
┌───────────────────────────────────────┐
│         Database Layer                │
├───────────────────────────────────────┤
│  ├─ MySQL (Production)                │
│  ├─ H2 (Development)                  │
│  ├─ Tables:                           │
│  │  ├─ users                          │
│  │  ├─ projects                       │
│  │  ├─ tasks                          │
│  │  ├─ github_repositories            │
│  │  ├─ git_commits                    │
│  │  └─ risk_alerts                    │
└───────────────────────────────────────┘

External Services:
├─ Gemini API (AI Task Generation)
├─ GitHub API (Repository & Commit Data)
└─ OAuth Providers
```

---

## ENTITIES & DATABASE SCHEMA

### User Table
```
users (PRIMARY: uid)
├─ uid (String, PK)
├─ name (String, NOT NULL)
├─ email (String, UNIQUE, NOT NULL)
└─ photoUrl (String)
```

### Project Table
```
projects (PRIMARY: id)
├─ id (Long, AUTO_INCREMENT)
├─ name (String, NOT NULL)
├─ description (TEXT)
├─ problemStatement (TEXT)
├─ createdBy (FK -> users.uid, NOT NULL)
├─ createdAt (DateTime, NOT NULL)
├─ updatedAt (DateTime, NOT NULL)
├─ githubRepoUrl (String)
├─ status (Enum: ACTIVE, COMPLETED, ARCHIVED, ON_HOLD)
└─ aiGeneratedFeatures (BLOB)
```

### Tasks Table
```
tasks (PRIMARY: id)
├─ id (Long, AUTO_INCREMENT)
├─ title (String, NOT NULL)
├─ description (TEXT)
├─ assignedTo (FK -> users.uid, NOT NULL)
├─ status (Enum: PENDING, IN_PROGRESS, COMPLETED, BLOCKED, CANCELLED)
├─ priority (Enum: LOW, MEDIUM, HIGH, CRITICAL)
├─ createdAt (DateTime, NOT NULL)
├─ updatedAt (DateTime)
├─ dueDate (DateTime)
├─ estimatedHours (Double)
└─ completedAt (DateTime)
```

### GitHubRepository Table
```
github_repositories (PRIMARY: id)
├─ id (Long, AUTO_INCREMENT)
├─ repoName (String, NOT NULL)
├─ repoUrl (String, NOT NULL)
├─ owner (String, NOT NULL)
├─ description (TEXT)
├─ projectId (FK -> projects.id)
├─ userId (FK -> users.uid, NOT NULL)
├─ createdAt (DateTime, NOT NULL)
├─ lastSynced (DateTime)
├─ stars (Integer)
├─ forks (Integer)
└─ openIssues (Integer)
```

### GitCommit Table
```
git_commits (PRIMARY: id)
├─ id (Long, AUTO_INCREMENT)
├─ commitHash (String, NOT NULL)
├─ author (String, NOT NULL)
├─ message (TEXT)
├─ commitDate (DateTime, NOT NULL)
├─ repositoryId (FK -> github_repositories.id, NOT NULL)
├─ mappedTaskId (FK -> tasks.id)
├─ createdAt (DateTime)
├─ additions (Integer)
├─ deletions (Integer)
└─ fileChanges (Integer)
```

### RiskAlert Table
```
risk_alerts (PRIMARY: id)
├─ id (Long, AUTO_INCREMENT)
├─ projectId (FK -> projects.id, NOT NULL)
├─ riskType (Enum: NO_COMMITS_IN_HOURS, TOO_MANY_PENDING_TASKS, MISSED_DEADLINE, etc.)
├─ description (TEXT)
├─ severity (Enum: LOW, MEDIUM, HIGH, CRITICAL)
├─ detectedAt (DateTime, NOT NULL)
├─ resolvedAt (DateTime)
├─ status (Enum: ACTIVE, ACKNOWLEDGED, RESOLVED)
└─ suggestedAction (TEXT)
```

---

## BUILD & DEPLOYMENT

### Maven Build
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package as JAR
mvn clean package

# Run with Maven
mvn spring-boot:run
```

### Build Configuration
- Java Version: 21
- Spring Boot: 4.0.5
- Build Tool: Maven
- Lombok: Enabled with annotation processors
- Database: H2 (dev), MySQL (production)

### Running the Application
```bash
cd ai
mvn spring-boot:run

# Or
java -jar target/ai-0.0.1-SNAPSHOT.jar
```

Server will start at: `http://localhost:8080/api`

---

## CLEAN ARCHITECTURE PRINCIPLES APPLIED

1. **Separation of Concerns**
   - Controllers handle HTTP requests/responses
   - Services handle business logic
   - Repositories handle data access

2. **Dependency Injection**
   - All components use @Autowired
   - Interfaces for services (ISomethingService)
   - Loose coupling between layers

3. **DTOs for API Contract**
   - Input: Request DTOs (@PostMapping, @PutMapping)
   - Output: Response DTOs (always returned as JSON)
   - Decouples API from entity structures

4. **SOLID Principles**
   - S: Each class has single responsibility
   - O: Open for extension, closed for modification
   - L: Liskov Substitution (Service interfaces)
   - I: Interface Segregation (IAIService, IProjectService, etc.)
   - D: Dependency Inversion (depends on abstractions)

5. **Lombok Usage**
   - @Data: getters, setters, toString, equals, hashCode
   - @NoArgsConstructor: default constructor
   - @AllArgsConstructor: all-args constructor
   - Reduces boilerplate by ~70%

---

## ERROR HANDLING

All endpoints follow consistent error response format:

```json
{
  "error": "Descriptive error message"
}
```

HTTP Status Codes:
- 200 OK: Successful GET/POST/PUT
- 201 CREATED: Resource created
- 400 BAD REQUEST: Invalid input
- 404 NOT FOUND: Resource not found
- 500 INTERNAL_SERVER_ERROR: Server error

---

## NEXT STEPS FOR PRODUCTION

1. **GitHub OAuth Implementation**
   - Configure OAuth credentials
   - Implement token refresh logic
   - Secure token storage

2. **Gemini API Integration**
   - Set GEMINI_API_KEY environment variable
   - Add retry/fallback logic
   - Implement request rate limiting

3. **Database Setup**
   - Switch from H2 to MySQL
   - Run migration scripts
   - Configure connection pooling

4. **Frontend Integration**
   - Configure CORS origins
   - Implement API client in React
   - Add authentication flows

5. **Testing**
   - Unit tests for Services
   - Integration tests for Controllers
   - API contract tests

6. **Deployment**
   - Docker containerization
   - Kubernetes deployment manifests
   - CI/CD pipeline setup
