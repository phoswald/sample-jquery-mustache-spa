# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build and package
mvn clean verify

# Run in dev mode (hot reload)
mvn quarkus:dev

# Run tests only (unit)
mvn test

# Run integration tests (disabled by default via skipITs=true)
mvn verify -DskipITs=false

# Run standalone after build
export APP_TASK_DIRECTORY=./data
java -jar target/quarkus-app/quarkus-run.jar

# Build and run Docker image
docker build -f src/main/docker/Dockerfile -t sample-jquery-mustache-spa .
docker run -it --rm --name sample-jquery-mustache-spa \
  -e APP_TASK_DIRECTORY=/data \
  -v ./data:/data \
  -p 8080:8080 \
  sample-jquery-mustache-spa
```

## Architecture

This is a Quarkus REST backend with a jQuery + Mustache single-page frontend (no npm/node toolchain — frontend assets are plain static files).

**Backend (Java)**
- `RestApplication` sets the JAX-RS base path to `/app`
- `TaskResource` exposes REST endpoints at `/app/rest/tasks`: `GET` (list with optional `skip`/`limit`), `POST` (create), `DELETE /{taskId}` (soft-delete)
- `TaskRepository` is the persistence layer — tasks are stored as individual JSON files in a directory configured via `app.task.directory` (env var `APP_TASK_DIRECTORY`). Deletion renames the file to `.json.old` rather than removing it.
- `Task` is a Lombok-annotated POJO with fields: `taskId`, `userId`, `timestamp`, `title`, `description`, `done`. `userId` is hardcoded to `"guest"` on creation.

**Frontend (static resources under `src/main/resources/META-INF/resources/`)**
- `index.html` — landing page with Bootstrap 5.2 navbar
- `tasks.html` — task CRUD page; embeds a Mustache template in a `<script type="text/html">` tag and renders into `#target-registrations`
- `tasks.js` — jQuery-driven: calls `POST /app/rest/tasks` to create tasks, `GET /app/rest/tasks` to refresh the list; uses `Mustache.render()` for templating. All CDN dependencies (Bootstrap, Mustache, jQuery) are loaded from jsDelivr.

**Configuration**
- `src/main/resources/application.properties`: sets `quarkus.http.cors=true` and defaults `app.task.directory=/tmp`
- The test suite uses `@QuarkusTest` + RestAssured and writes to `/tmp` (default config)

## Java and dependency notes

- Use `jakarta.*` imports throughout (not `javax.*`) — this project targets Quarkus 3.x which uses the Jakarta EE 10 namespace for JAX-RS, CDI, etc.
- Lombok requires `maven.compiler.proc=full` in `<properties>` because Java 23+ no longer discovers annotation processors from the classpath implicitly.
