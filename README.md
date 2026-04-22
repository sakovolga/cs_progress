# cs_progress

Progress Tracking Service for the Coding School platform. Listens to domain events from RabbitMQ and maintains a complete picture of each student's learning progress, achievements, and statistics.

## Responsibilities

- Consumes events published by other services (task completed, test resolved, lesson viewed, etc.)
- Persists progress at multiple levels: task, test, topic, tag, course
- Calculates completion percentages and statistics
- Manages achievements and milestones
- Provides dashboard data and AI-generated insights
- Exposes REST API for the frontend to query progress

## Tech Stack

| Category | Technology |
|----------|-----------|
| Framework | Spring Boot 3.3.0, Java 21 |
| Database | PostgreSQL (Spring Data JPA) |
| Messaging | Spring AMQP (RabbitMQ) |
| AI Insights | Spring AI + Ollama (`qwen2.5-coder:7b`) |
| Caching | Caffeine |
| Testing | Spock / Groovy |

## Domain Model

| Entity | Table | Description |
|--------|-------|-------------|
| `TaskProgress` | `tasks_progress` | Per-task status, score, code snapshot, AI rating |
| `CourseOverview` | `course_overviews` | Aggregated course completion data |
| `TagProgress` | `tag_progress` | Progress by skill tag |
| `TestItemResult` | — | Individual test question results |
| `LastTopic` | `last_topics` | Most recently visited topic per user |

## RabbitMQ Event Listeners

| Queue | Listener | Trigger |
|-------|----------|---------|
| `task.completed.queue` | `TaskCompletedEventListener` | Student submits a passing solution |
| `test.item.resolved.queue` | `TestItemResolvedEventListener` | Student answers a test question |
| `lesson.viewed.queue` | `LessonViewedEventListener` | Student opens a lesson |
| `snapshot.sent.queue` | `SnapshotSentEventListener` | Code snapshot autosaved |
| `ai.rating.updated.queue` | `AIRatingUpdatedEventListener` | Runner completes AI code review |

Dead-letter queues are configured for `test.item.resolved` and `lesson.viewed` with max 3 retries.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/topic` | Get user's last visited topic |
| GET | `/api/test-progress` | Current test progress |
| POST | `/api/test-progress/finish` | Finish a test and record results |
| GET | `/api/task-progress` | Task progress details |
| GET | `/api/task-progress/list` | All tasks progress for a topic |
| GET | `/api/task-progress/by-task-ids` | Bulk task progress by IDs |
| POST | `/api/task-progress/autosave` | Save a code snapshot |
| GET | `/api/dashboard` | User dashboard summary |
| GET | `/api/dashboard/ai-insights/{userId}` | AI-generated learning insights |
| GET | `/api/dashboard/topics-tab` | Topics progress breakdown |
| GET | `/api/dashboard/tags-tab` | Tags/skills progress breakdown |
| GET | `/api/dashboard/completed-topics` | List of completed topics |
| GET | `/api/course-completion` | Course completion status |
| POST | `/api/course-overview` | Create or update course overview |

## Configuration

```env
SERVER_PORT=8083

# PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/cs_progress
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
```

### RabbitMQ Listener Settings

- Concurrency: 3–10 consumers
- Prefetch: 10 messages
- Retry: enabled, 2 s initial interval, max 3 attempts

### Ollama AI Insights

- Base URL: `http://localhost:11434`
- Model: `qwen2.5-coder:7b`

## Running Locally

```bash
./mvnw spring-boot:run
```

Requires PostgreSQL and RabbitMQ to be running. Ollama is optional (used only for AI insights).