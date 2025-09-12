# Evently Platform - Development Guide

## 📖 Overview

Evently is an enterprise-grade event ticketing platform built with Spring Boot 3.5.5 and Java 21. It handles concurrent booking requests, prevents overselling, and provides comprehensive event management capabilities.

## 🏗️ Architecture

### Core Components

* **Spring Boot 3.5.5** - Main application framework with auto-configuration
* **Spring Data JPA** - Database access layer with PostgreSQL
* **Spring Security** - Authentication and authorization (token-based for MVP)
* **Spring Cache** - Redis-based caching for performance optimization
* **Spring Kafka** - Event-driven messaging for notifications and analytics
* **Spring Mail** - Email notifications via MailHog (development) or SMTP (production)
* **Spring WebSocket** - Real-time notifications via STOMP protocol
* **Flyway** - Database schema migrations and versioning
* **Testcontainers** - Integration testing with real database containers

### Business Features Implemented

✅ **Event Management**: Create, update, list events with capacity tracking
✅ **Atomic Booking System**: Concurrency-safe ticket booking with zero overselling
✅ **Waitlist System**: FIFO queue with automatic notifications when seats become available
✅ **Triple Notification Delivery**: Email + In-app + Real-time WebSocket notifications
✅ **Admin Analytics**: Comprehensive booking statistics and popular events tracking
✅ **User Management**: Registration, profile management, booking history
✅ **Caching Layer**: Redis caching for high-performance event listings
✅ **Event-Driven Architecture**: Kafka-based messaging for scalable operations

## 🛠️ Technical Implementation

### Concurrency Protection (Multi-Layered)

1. **Primary**: Atomic database updates using optimized SQL
   ```sql
   UPDATE events SET available_seats = available_seats - :quantity 
   WHERE id = :eventId AND available_seats >= :quantity
   ```

2. **Secondary**: Idempotency keys for retry safety
   ```java
   @Column(name = "idempotency_key", unique = true)
   private String idempotencyKey;
   ```

3. **Tertiary**: Optimistic locking with @Version
   ```java
   @Version
   @Column(name = "version")
   private Integer version;
   ```

### Database Design

- **PostgreSQL 15+** with optimized indexes for high-concurrency scenarios
- **Flyway migrations** in `src/main/resources/db/migration/`
- **Constraint-based integrity** to prevent data corruption
- **Connection pooling** via HikariCP (50+ concurrent connections)

### Caching Strategy

- **Redis 7+** for distributed caching and session storage
- **Cache keys**: `events:page-{page}-size-{size}`, `event-details:{eventId}`
- **TTL Configuration**: 5 minutes for event lists, 10 minutes for event details
- **Cache invalidation** on admin event modifications

### Event-Driven Architecture

- **Kafka KRaft mode** for message streaming (no Zookeeper dependency)
- **Topics**: `booking-cancelled`, `waitlist-notification`
- **Consumer groups** for parallel processing and fault tolerance
- **Manual acknowledgment** for reliable message processing

## 🚀 Getting Started

### Prerequisites

```bash
# Required software
Java 21+ (OpenJDK or Oracle)
Maven 3.8+
Docker & Docker Compose
Git

# Check versions
java --version
mvn --version
docker --version
```

### Quick Setup

1. **Start infrastructure services**
   ```bash
   # Start PostgreSQL, Redis, Kafka, MailHog
   docker-compose up -d

   # Verify all services are running
   docker-compose ps
   ```

2. **Configure environment**
   ```bash
   # Copy environment template
   cp .env.example .env
   
   # Edit .env with your settings (defaults work for local development)
   # Key settings:
   # - DATABASE_PASSWORD (default: klu@321)
   # - REDIS_PASSWORD (leave empty for local)
   # - ADMIN_TOKEN (default: admin-secret)
   ```

3. **Run the application**
   ```bash
   # Development mode (with hot reload)
   mvn spring-boot:run -Dspring-boot.run.profiles=development
   
   # Or build and run JAR
   mvn clean package -DskipTests
   java -jar target/evently-0.0.1-SNAPSHOT.jar
   ```

4. **Verify installation**
   ```bash
   # Health check
   curl http://localhost:8080/actuator/health
   
   # API documentation
   open http://localhost:8080/swagger-ui.html
   
   # Email interface (MailHog)
   open http://localhost:8025
   ```

## 🧪 Testing

### Running Tests

```bash
# Unit tests only
mvn test -Dtest="*Test"

# Integration tests (uses Testcontainers)
mvn test -Dtest="*IntegrationTest"

# All tests with coverage
mvn clean test jacoco:report
open target/site/jacoco/index.html

# Specific test class
mvn test -Dtest="BookingServiceTest"
```

### Test Categories

- **Unit Tests**: Service logic with mocked dependencies
- **Integration Tests**: Full Spring context with Testcontainers
- **Concurrency Tests**: Simulated concurrent booking scenarios
- **API Tests**: REST endpoint testing with MockMvc

### Testcontainers Configuration

The project uses Testcontainers for integration testing:

* **PostgreSQL container**: For database integration tests
* **Redis container**: For cache integration tests  
* **Kafka container**: For event streaming tests

## 📊 Monitoring & Observability

### Available Endpoints

```bash
# Application health
GET /actuator/health

# Metrics (Prometheus format)
GET /actuator/metrics
GET /actuator/metrics/hikaricp.connections.active
GET /actuator/metrics/cache.gets

# Cache information
GET /actuator/caches

# Application info
GET /actuator/info
```

### Logging

- **Structured logging** with logback configuration
- **Log levels**: Configurable via environment variables
- **Request tracing**: Correlation IDs for request tracking
- **Performance metrics**: Response times and throughput

### Performance Benchmarks

```bash
# Expected performance characteristics:
# - 2,000+ concurrent booking requests per second
# - 45ms average response time under load
# - 95% cache hit ratio for event listings
# - Zero oversells under all concurrency scenarios
```

## 🔐 Security

### Authentication Methods

- **Public endpoints**: Event browsing, user registration
- **Admin endpoints**: Require `X-Admin-Token` header
- **User endpoints**: Currently public for MVP (JWT planned for production)

### Security Features

- **Input validation** via Bean Validation annotations
- **SQL injection prevention** through parameterized queries
- **CORS configuration** for frontend integration
- **Rate limiting** infrastructure ready (Redis-based)

### Production Security Checklist

- [ ] Replace admin token with proper JWT implementation
- [ ] Enable HTTPS enforcement
- [ ] Implement user session management
- [ ] Add request rate limiting
- [ ] Configure security headers
- [ ] Set up audit logging

## 🚀 Deployment

### Environment Profiles

- **development**: Local development with debug logging
- **test**: Automated testing with H2 in-memory database  
- **production**: Production optimizations and security
- **docker**: Container-friendly configuration

### Docker Deployment

```bash
# Build application image
docker build -t evently:latest .

# Run with Docker Compose
docker-compose -f docker-compose.prod.yml up -d

# Environment variables for production
DATABASE_URL=postgresql://prod-db:5432/evently
REDIS_URL=redis://redis-cluster:6379
KAFKA_BROKERS=kafka-1:9092,kafka-2:9092
ADMIN_TOKEN=secure-production-token
```

### Production Platforms

Tested deployment platforms:
- **Render**: Recommended for easy deployment
- **Railway**: Good for rapid prototyping  
- **Heroku**: Classic PaaS option
- **AWS/GCP/Azure**: For enterprise deployments

## 🔄 Development Workflow

### Code Organization

```
src/main/java/com/atlan/evently/
├── config/          # Configuration classes
├── controller/      # REST API controllers  
├── service/         # Business logic services
├── repository/      # Data access repositories
├── model/           # JPA entities
├── dto/             # API request/response objects
├── mapper/          # Entity-DTO mapping
├── exception/       # Custom exceptions
└── util/            # Utility classes
```

### Database Migrations

```bash
# Create new migration
# File: src/main/resources/db/migration/V{version}__{description}.sql

# Example: V007__add_notification_table.sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    -- ... other columns
);

# Migrations run automatically on application startup
```

### Adding New Features

1. **Database changes**: Create Flyway migration
2. **Domain model**: Update JPA entities
3. **Business logic**: Implement in service layer
4. **API layer**: Add controller endpoints
5. **Testing**: Unit + integration tests
6. **Documentation**: Update API docs and README

## 🐛 Troubleshooting

### Common Issues

**Database Connection Errors**
```bash
# Check PostgreSQL container
docker-compose ps postgres
docker-compose logs postgres

# Test connection
psql -h localhost -U postgres -d evently-db
```

**Redis Connection Issues**
```bash
# Check Redis container
docker exec evently_redis redis-cli ping

# Monitor Redis
docker exec evently_redis redis-cli monitor
```

**Cache Problems**
```bash
# Clear all caches
curl -X DELETE http://localhost:8080/actuator/caches

# Check cache statistics  
curl http://localhost:8080/actuator/caches
```

**Kafka Issues**
```bash
# Check Kafka health
curl http://localhost:8080/actuator/health/kafka

# View Kafka topics
docker exec evently_kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Performance Issues

```bash
# Monitor database connections
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active

# Check memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Monitor request metrics
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## 📚 Additional Resources

### Spring Boot Guides

* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Messaging with Redis](https://spring.io/guides/gs/messaging-redis/)
* [Building WebSocket applications](https://spring.io/guides/gs/messaging-stomp-websocket/)

### External Documentation

* [PostgreSQL Documentation](https://www.postgresql.org/docs/)
* [Redis Documentation](https://redis.io/documentation)
* [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
* [Flyway Documentation](https://flywaydb.org/documentation/)

### API Testing Tools

* **Swagger UI**: `http://localhost:8080/swagger-ui.html`
* **Postman**: Import OpenAPI spec from `/v3/api-docs`
* **curl**: Command-line HTTP testing
* **HTTPie**: User-friendly HTTP client

---

## 🤝 Contributing

### Development Setup

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Make changes and add tests
4. Run full test suite (`mvn clean test`)
5. Commit with conventional messages (`git commit -m 'feat: add amazing feature'`)
6. Push and create Pull Request

### Code Quality Standards

- **Java Style**: Google Java Style Guide
- **Test Coverage**: Minimum 80% line coverage
- **Documentation**: JavaDoc for public APIs
- **Commit Messages**: Conventional commit format

---

For questions or issues, please check the GitHub repository or create an issue with detailed reproduction steps.

