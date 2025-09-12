# Running profiles checklist (quick)

This checklist explains how to run the application for each profile and what to verify.

1) Development (local)
- Files: .env.development
- Start infra (recommended):
  docker-compose -f docker-compose.override.yml up -d
- Build & run:
  mvn spring-boot:run -Dspring-boot.run.profiles=development
- Verify:
  - http://localhost:8080/actuator/health -> {"status":"UP"}
  - Swagger UI: http://localhost:8080/swagger-ui.html
  - MailHog UI: http://localhost:8025 (if using mailhog)
- Notes:
  - REDIS_PASSWORD must be empty/unset locally to avoid AUTH error.
  - Use admin header for admin endpoints: X-Admin-Token: admin-secret

2) Docker compose (containerized dev)
- Files: .env.docker (used by docker-compose.override.yml)
- Start:
  docker-compose -f docker-compose.override.yml up --build
- Verify:
  - App logs: docker-compose logs -f evently-app
  - DB connectivity: psql -h localhost -p 5432 -U postgres -d evently-db

3) Test (CI / unit & integration)
- Files: .env.test
- Run tests (prefer CI/Testcontainers):
  mvn -Dspring.profiles.active=test test
- Verify:
  - Tests pass locally and in CI.
  - Integration tests use Testcontainers or H2 (configured in profile).

4) Production (deployment platform)
- Files: .env.production (do NOT commit; set via host/CI secrets)
- Deploy:
  - Build image: docker build -t evently:latest .
  - Push to registry and deploy using platform (Render/Railway/K8s)
  - Or use docker-compose.prod.yml in a controlled environment:
    docker-compose -f docker-compose.prod.yml up -d
- Verify:
  - Health endpoint returns UP
  - Metrics exposed for Prometheus
  - Ensure all env secrets (DB password, Redis password, SMTP creds, JWT secret) are injected via the platform's secret manager

Common troubleshooting
- Redis AUTH error (ERR AUTH called without password):
  - Ensure REDIS_PASSWORD is unset or empty in development (.env.development/.env.docker).
  - In application.yml Redis password uses ${REDIS_PASSWORD:#{null}} so empty value won't send AUTH.
- Kafka connectivity:
  - Verify KAFKA_BOOTSTRAP_SERVERS matches broker host:port.
- DB migrations:
  - Flyway runs on startup. Check logs for migration errors.

Tips
- Never commit .env.production or real secrets.
- Use platform secrets / CI variables for production values.
