# 🧠 **Title**

**AI-Powered Hackathon Assistant for Real-Time Team Productivity**

---

# 📝 **Description**

An intelligent system that assists hackathon teams by transforming problem statements into structured tasks, tracking development progress through GitHub integration, analyzing team performance, and providing real-time AI-driven suggestions to improve productivity and ensure timely project completion. The system also generates a final presentation summary automatically.

---

# ⚙️ **Usage**

* User inputs hackathon problem statement
* AI generates:

  * Features
  * Task breakdown
* Team members:

  * Assign tasks
  * Connect GitHub repo
* System:

  * Tracks commits and maps to tasks
  * Shows progress and risks
  * Suggests next actions
* At the end:

  * Generates presentation summary

---

# 🛠️ **Required Stack**

### Backend

* Spring Boot
* REST APIs
* JPA / Hibernate

### Frontend

* React.js (or simple HTML if time is crying)

### AI Layer

* Gemini API

### Integration

* GitHub API (OAuth + repo access)

### Database

* MySQL / PostgreSQL

### Optional (if you feel heroic)

* WebSockets (real-time updates)
* Redis (caching)

---

# 🧩 **Task Division**

## 👤 Member 1 – Backend Core

* Setup Spring Boot project
* Create APIs:

  * Task management
  * User management
* DB schema design
* Implement service layer (SOLID)

---

## 👤 Member 2 – AI Integration

* Integrate Gemini API
* Build:

  * Problem → feature extractor
  * Task generator
* Prompt engineering
* Response parsing → structured JSON

---

## 👤 Member 3 – GitHub Integration

* Implement OAuth login
* Fetch:

  * repos
  * commits
* Map commits to tasks
* Build progress tracker

---

## 👤 Member 4 – Frontend

* UI for:

  * Input problem
  * Display tasks
  * Show progress
* Dashboard (basic but clean)

---

## 👤 Member 5 (Optional) – Intelligence Layer

* Risk detection logic
* Suggestion engine
* Presentation generator

(If you don’t have 5 people, congratulations, you get extra suffering.)

---

# 🏗️ **Setup Plan (Execution Flow)**

## 🔹 Step 1: Project Initialization

* Create Spring Boot project
* Setup DB
* Basic APIs working

---

## 🔹 Step 2: AI Integration

* Connect Gemini
* Build:

  * prompt → response → JSON

---

## 🔹 Step 3: Task Module

* Create:

  * Task entity
  * APIs (CRUD)
* Store AI-generated tasks

---

## 🔹 Step 4: GitHub Integration

* Implement OAuth
* Fetch repo data
* Store commits

---

## 🔹 Step 5: Progress Engine

* Map commits → tasks
* Calculate completion %

---

## 🔹 Step 6: Dashboard

* Show:

  * tasks
  * progress
  * team activity

---

## 🔹 Step 7: Intelligence Layer (if time allows)

* Risk alerts
* AI suggestions
* Auto presentation generator

---
