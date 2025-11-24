# Running with Docker

This project supports two modes:

## Mode 1: Local Development (Database in Docker, App Native)

**Use this when developing locally for fast feedback:**

```bash
# Start only database
docker-compose up -d

# Run app natively
./mvnw spring-boot:run
```

**Benefits:**
- Fast app restarts
- Easy debugging
- Hot reload support

---

## Mode 2: Full Docker (Everything in Docker)

**Use this to share with others or deploy:**

```bash
# Start everything (database + app)
docker-compose --profile full up -d
```

That's it! App runs at `http://localhost:8080`

**To rebuild after code changes:**
```bash
docker-compose --profile full up -d --build
```

---

## Commands Summary

```bash
# Local development (DB only)
docker-compose up -d                          # Start database
./mvnw spring-boot:run                        # Run app natively

# Full Docker (DB + App)
docker-compose --profile full up -d           # Start everything
docker-compose --profile full up -d --build   # Rebuild and start

# Running Tests (Docker only, no Java/Maven needed)
docker-compose --profile test up --build test # Run all tests
docker-compose --profile test run --rm test ./mvnw test -Dtest=ReservationIntegrationTests

# Stop everything
docker-compose down                           # Stop all containers
docker-compose down -v                        # Stop and remove data
docker-compose --profile test down            # Stop test containers
```

---

## Mode 3: Running Tests with Docker

**Run tests in Docker without installing Java/Maven:**

```bash
# Easy way: Use the convenience script (auto-cleanup included)
./test-docker.sh  # Run all tests
./test-docker.sh -Dtest=ReservationIntegrationTests  # Run specific tests

# Manual way: Using docker-compose directly
docker-compose --profile test run --rm test ./mvnw test
docker-compose --profile test run --rm test ./mvnw test -Dtest=ReservationIntegrationTests
docker-compose --profile test run --rm test ./mvnw test -Dtest=ReservationIntegrationTests#"should expire reservations after hold time"
```

**How it works:**
- Tests run inside a Docker container with Java 21 and Maven pre-installed
- Container accesses Docker daemon via socket mount to spin up test containers
- Testcontainers creates PostgreSQL containers for integration testing
- Maven dependencies are cached in a Docker volume for faster subsequent runs
- Ryuk (Testcontainers cleanup) is disabled on macOS due to networking limitations

**Cleanup after tests:**
```bash
# Stop test container
docker-compose --profile test down

# Clean up Testcontainers (PostgreSQL test containers)
docker ps -a | grep testcontainers | awk '{print $1}' | xargs docker rm -f
docker volume prune -f

# Clean up Maven cache (optional, saves ~200MB)
docker volume rm booking-service_maven-cache
```

---

## Sharing with Others

Just share the project folder. They can run:

```bash
# Run the app
docker-compose --profile full up -d

# Run tests
docker-compose --profile test up test
```

No need to install Java, Maven, or anything else! Just Docker.

---

## Quick Reference

| What | Command | Prerequisites |
|------|---------|---------------|
| **Development** | `docker-compose up -d && ./mvnw spring-boot:run` | Docker + Java/Maven |
| **Production** | `docker-compose --profile full up -d` | Docker only |
| **Testing** | `docker-compose --profile test up test` | Docker only |
