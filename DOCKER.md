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
docker-compose up -d                      # Start database
./mvnw spring-boot:run                    # Run app natively

# Full Docker (DB + App)
docker-compose --profile full up -d       # Start everything
docker-compose --profile full up -d --build  # Rebuild and start

# Stop everything
docker-compose down                       # Stop all containers
docker-compose down -v                    # Stop and remove data
```

---

## Sharing with Others

Just share the project folder. They can run:

```bash
docker-compose --profile full up -d
```

No need to install Java, Maven, or anything else! Just Docker.
