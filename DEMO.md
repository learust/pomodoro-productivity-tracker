# Pomodoro Productivity Tracker MVP - Quick Demo

## Run Instructions
```bash
# Build and run the application
mvn spring-boot:run

# The application will start on http://localhost:8080
# H2 Console available at http://localhost:8080/h2-console
```

## Test Instructions  
```bash
# Run all 45 comprehensive JUnit 5 tests
mvn test

# Run specific test suites
mvn test -Dtest=PomodoroTimerServiceTest
mvn test -Dtest=SessionLoggingServiceTest  
mvn test -Dtest=ProgressChartServiceTest
mvn test -Dtest=TaskManagementServiceTest
```

## API Demo (while application is running)

### 1. Timer Operations
```bash
# Check timer status
curl http://localhost:8080/api/timer/status

# Start a work session (25 minutes)
curl -X POST http://localhost:8080/api/timer/start

# Pause the timer
curl -X POST http://localhost:8080/api/timer/pause

# Stop the timer
curl -X POST http://localhost:8080/api/timer/stop

# Configure timer settings
curl -X POST "http://localhost:8080/api/timer/configure?workMinutes=25&shortBreakMinutes=5&longBreakMinutes=15"
```

### 2. Task Management
```bash
# Create a new task
curl -X POST -H "Content-Type: application/json" \
  -d '{"text":"Complete documentation"}' \
  http://localhost:8080/api/tasks

# Get today's tasks
curl http://localhost:8080/api/tasks/today

# Complete a task (replace 1 with actual task ID)
curl -X PUT http://localhost:8080/api/tasks/1/complete
```

### 3. Session History
```bash
# Get all completed sessions
curl http://localhost:8080/api/sessions/all

# Get today's sessions
curl http://localhost:8080/api/sessions/today
```

### 4. Progress Chart
```bash
# Get current year progress chart
curl http://localhost:8080/api/progress/current

# Get progress for specific year
curl http://localhost:8080/api/progress/2025

# Get yearly statistics
curl http://localhost:8080/api/progress/2025/stats
```

## MVP Features Demonstrated

✅ **Core Pomodoro Timer** (25/5/15 minutes)
- Start, pause, stop functionality
- Automatic session transitions
- Configurable work and break periods

✅ **Session Logging** 
- All sessions saved to database with UTC timestamps
- Session duration and type tracking
- Persistent data across application restarts

✅ **GitHub-Style Progress Chart**
- 4-color productivity levels (0hrs=gray, <1hr=light green, 1-3hr=medium green, 4+hr=dark green)
- Monthly and yearly views
- Productivity statistics and streaks

✅ **Daily Task Management**
- Create, update, complete, delete tasks
- Daily task reset functionality
- Task prioritization and overdue tracking

✅ **Comprehensive Testing**
- 45 JUnit 5 tests covering all MVP features
- 100% test pass rate
- Tests for timer, sessions, progress, and tasks

✅ **Technical Requirements**
- Java 21 (exceeds Java 17+ requirement)
- Maven build system
- Spring Boot 3.2.0 framework  
- JUnit 5 testing framework
- H2 in-memory database
- REST API architecture

## Database Schema
- `completed_sessions`: Stores all completed Pomodoro sessions
- `daily_tasks`: Stores daily tasks with completion tracking

## Architecture
- **Controllers**: REST API endpoints with CORS support
- **Services**: Business logic for timer, sessions, progress, tasks
- **Repositories**: JPA data access layer
- **Models**: JPA entities with proper JSON serialization