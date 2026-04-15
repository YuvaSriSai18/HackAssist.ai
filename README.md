# 🚀 HackAssist - AI-Powered Hackathon Assistant

![HackAssist Logo](https://res.cloudinary.com/dvbsion81/image/upload/v1776254287/cf482d76-fce9-4f1c-9a04-26bed2054f88_r4har1.png)
## Problem Statement

Hackathon teams struggle with:
- **Project Management**: Tracking tasks, features, and project status without proper tools
- **Code Insights**: Understanding project structure and identifying risks/roadblocks without AI assistance
- **GitHub Integration**: Manually fetching commits, tracking progress, and webhook management
- **Team Coordination**: Monitoring individual contributions and task dependencies
- **Risk Detection**: Identifying bottlenecks, missed deadlines, and team inactivity early

**Result**: Teams lose time managing instead of building, miss critical issues, and struggle with visibility.

---

## Solution

**HackAssist** is an **AI-powered hackathon assistant** that:

✅ **Automates Project Planning** - AI generates project features, tasks, and evaluation criteria  
✅ **Tracks Progress Real-Time** - GitHub webhook integration for automatic commit tracking  
✅ **Detects Risks Early** - AI identifies stuck tasks, missed deadlines, inactive team members  
✅ **Provides AI Insights** - Gemini AI generates summaries, recommendations, and risk analysis  
✅ **Manages Tasks Efficiently** - Kanban boards, task dependencies, and automated status updates  
✅ **GitHub Integration** - OAuth2, webhook management, and commit evaluation

---

## Key Features

### 🎯 Core Features
- **Project Creation & Management** - Create projects with problem statements and AI-generated features
- **Task Management** - Create, track, and manage project tasks with dependencies
- **GitHub Integration** - Connect repositories, track commits, evaluate task completion
- **Risk Monitoring** - Automatic detection of project risks and alerts
- **Dashboard** - Real-time insights, Kanban board, team panel, and risk alerts
- **AI Evaluation** - Gemini AI evaluates project structure and provides recommendations

### 🔐 Security
- OAuth2 authentication (Google, GitHub)
- JWT token-based authorization
- PostgreSQL database with Supabase
- Environment-based configuration (no hardcoded secrets)

### 📱 Tech Stack
- **Frontend**: React 18 + TypeScript + Vite + TailwindCSS
- **Backend**: Spring Boot 4.0.5 + Java 21 + PostgreSQL
- **Database**: Supabase PostgreSQL
- **Deployment**: 
  - Backend: Render (Docker containerized)
  - Frontend: Vercel
- **CI/CD**: GitHub Actions (automated Docker builds)

---

## Who Is This For?

### 👥 Primary Users
- **Hackathon Participants** - Teams building projects during hackathons
- **Project Managers** - Teams needing real-time project insights
- **Developers** - Need automated task tracking and GitHub integration
- **Hackathon Organizers** - Monitor multiple teams and their progress

### 📋 Use Cases
1. **Pre-Hackathon**: Plan project structure and break down problem statement into tasks
2. **During Hackathon**: Track progress, identify risks, stay coordinated
3. **Post-Hackathon**: Review project evaluation, analyze team performance

---

## Acknowledgments

Built with ❤️ for hackathon teams by developers who struggled with project management.

Special thanks to:
- Supabase for PostgreSQL hosting
- Render for backend deployment
- Vercel for frontend deployment
- Google & GitHub for OAuth integration

---

**Happy Hacking! 🚀**
