# PayRoute — Payment Orchestration & Intelligent Routing Platform

PayRoute is an educational payment orchestration simulation platform featuring intelligent routing, state machine guarantees, idempotency, provider fallback, circuit breakers, rate limiting, and end-to-end observability.

> **Note:** This is an educational simulation. No real money, real banks, or real payment credentials are ever processed. All providers are fully simulated with configurable latencies and failure distributions.

---

## System Architecture

- **Backend**: Java 17, Spring Boot 3.2.3, Spring Security (JWT), Spring Data JPA, Hibernate, Flyway.
- **Database**: PostgreSQL 16 (strict ACID idempotency, row-level locking, state machine history).
- **Cache & Rate Limiter**: Redis 7 (Token Bucket rate limiter, provider health cache, idempotency cache).
- **Event Streaming**: Apache Kafka 3.7 (KRaft mode, topic `payment.events`, Dead Letter Topic `payment.dlq`).
- **Pure Functional Rules Engine**: Haskell (Scotty HTTP service on port 8081 with pure ADT evaluation).
- **Resilience**: Resilience4j Circuit Breaker & Retry with exponential backoff.
- **Observability**: Spring Boot Actuator, Micrometer custom metrics, Prometheus, Grafana.
- **Frontend**: Next.js 14, React 18, TypeScript, Tailwind CSS, Lucide icons.

---

## Quick Start (Docker Compose)

The entire platform can be launched with a single command:

```bash
docker compose up --build
```

### Services & Ports
- **Frontend Dashboard**: [http://localhost:3000](http://localhost:3000)
- **Backend REST API & Swagger**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Actuator Health & Metrics**: [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)
- **Haskell Rules Engine**: [http://localhost:8081/health](http://localhost:8081/health)
- **Prometheus**: [http://localhost:9090](http://localhost:9090)
- **Grafana**: [http://localhost:3001](http://localhost:3001) (Credentials: `admin` / `admin`)
- **PostgreSQL**: `localhost:5432` (`payroute` / `payroute_secret`)
- **Redis**: `localhost:6379`
- **Kafka**: `localhost:9092`

---

## Seed Accounts

When the database initializes, default test accounts are seeded:

| Role | Username | Password | Email |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `Admin@123456` | `admin@payroute.dev` |
| **Merchant** | `merchant_apex` | `Merchant@123456` | `merchant@apex.dev` |
| **User** | `customer_alice` | `User@123456` | `alice@customer.dev` |

---

## Running Locally Without Full Docker

### 1. Start Infrastructure (Postgres, Redis, Kafka)
```bash
docker compose up -d postgres redis kafka
```

### 2. Backend (Spring Boot 3 / Java 17)
```bash
cd backend
./mvnw clean spring-boot:run
```
*(On Windows: `mvnw.cmd clean spring-boot:run`)*

### 3. Frontend (Next.js)
```bash
cd frontend
npm install
npm run dev
```

### 4. Running Backend Tests
```bash
cd backend
./mvnw clean test
```
*(Runs JUnit 5 unit, state machine, routing, and concurrency idempotency tests).*
