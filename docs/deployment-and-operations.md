# Deployment and Operations Runbook

This document provides operational guidelines, backup strategies, and deployment configurations for the e-commerce platform.

## Infrastructure Architecture

The platform relies on the following infrastructure components:
- **PostgreSQL**: Primary relational database for transactions (Orders, Inventory, Users).
- **MongoDB**: Document database for high-volume unstructured/semi-structured data (Chat Messages, Products, Reviews).
- **Redis**: Distributed caching (Flash Sales, JWT tokens, Rate Limiting).
- **Kafka / Zookeeper**: Message broker for asynchronous event-driven architecture (Order Processing, Notifications).
- **Elasticsearch**: Full-text search engine for Product Catalogs.

## Environment Variables

Ensure the following variables are set in your deployment environment (`.env` or Kubernetes ConfigMaps/Secrets):

```env
# Database Configurations
SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/ecommerce
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<secure-password>

# MongoDB
SPRING_DATA_MONGODB_URI=mongodb://<mongo-host>:27017/ecommerce

# Cache & Messaging
SPRING_DATA_REDIS_HOST=<redis-host>
SPRING_DATA_REDIS_PORT=6379
SPRING_KAFKA_BOOTSTRAP_SERVERS=<kafka-host>:9092

# Search
SPRING_ELASTICSEARCH_URIS=http://<elastic-host>:9200

# Security
JWT_SECRET=<strong-random-hmac-secret-key>

# External APIs
SPRING_MAIL_HOST=<smtp-host>
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=<smtp-user>
SPRING_MAIL_PASSWORD=<smtp-pass>
```

## Database Migrations (Flyway)

All relational database schema changes are managed by Flyway. Migrations are executed automatically upon application startup.

### Fresh Database Setup
For a fresh environment, the application will automatically run all migrations in order: `V1__baseline.sql` -> `V2__phase2_schema_updates.sql` -> `V3__soft_delete_products.sql`.

### Migration Policy and Rules
To ensure safety and compatibility across local, staging, and production environments, the team must follow these rules:

1. **Immutability of Migrations**: Existing migration files (`V1`, `V2`, `V3`, etc.) are **strictly immutable** once they have been merged or deployed. Do not edit, rename, or delete them.
2. **Adding New Schema Changes**: All future schema modifications (adding tables, altering columns, adding indexes) must be defined in a **new, sequentially numbered migration file** (e.g., `V4__add_new_feature.sql`) under `backend/bootstrap/src/main/resources/db/migration/`.
3. **Legacy Data Compatibility**: Schema alterations must be safe for existing data. For example, converting column types from `VARCHAR` to `NUMERIC` must use conditional casting expressions (like `USING NULLIF(price, '')::numeric(19,2)`) to handle empty strings, whitespaces, and existing values without throwing errors.

### Checksum Drift & Local Dev Database Reset
Because `V1__baseline.sql` was consolidated, local development databases that ran on older versions of V1 will fail Flyway's checksum validation (`validate-on-migrate` checksum drift).
To fix this locally:
- **Option A (Recommended)**: Drop the local schema and re-create a clean database:
  ```bash
  dropdb -U postgres ecommerce
  createdb -U postgres ecommerce
  ```
  Then restart the Spring Boot application, and Flyway will rebuild the schema cleanly.
- **Option B (Repair Checksums)**: If you cannot drop your database, you can synchronize the schema history checksums:
  ```bash
  # Delete the V1 schema history entry so Flyway rebuilds its baseline metadata
  DELETE FROM flyway_schema_history WHERE version = '1';
  ```
  And then execute Flyway repair via Flyway CLI or the Gradle plugin.

### Staging & Production Migration Failures
If a migration fails in staging or production:
1. **Never** manually delete successful rows or blindly run `DELETE FROM flyway_schema_history` on production systems.
2. Inspect the exact step that failed and the current database state using your monitoring tools.
3. Manually revert any partially applied DDL/DML statements that caused the failure, ensuring the schema matches the state before the failed migration.
4. Execute `flyway repair` using Flyway CLI or the database migration runbook to recalculate checksums and mark the failed migration status as cleared.
5. Correct the SQL in the failed migration file (only if it was never successfully applied anywhere else) or apply a forward-only fix migration, and run migrate again.

## Backup & Restore Strategies

### PostgreSQL (Transactional Data)
Perform daily backups using `pg_dump`.

**Backup:**
```bash
pg_dump -U postgres -F c -f /backup/ecommerce_pg_$(date +%F).dump ecommerce
```

**Restore:**
```bash
pg_restore -U postgres -d ecommerce -1 /backup/ecommerce_pg_$(date +%F).dump
```

### MongoDB (Document Data)
Perform daily backups using `mongodump`.

**Backup:**
```bash
mongodump --uri="mongodb://localhost:27017/ecommerce" --archive=/backup/ecommerce_mongo_$(date +%F).archive
```

**Restore:**
```bash
mongorestore --uri="mongodb://localhost:27017/ecommerce" --archive=/backup/ecommerce_mongo_$(date +%F).archive
```

## Monitoring and Observability

- **Metrics**: Exposes Prometheus metrics at `/actuator/prometheus`.
- **Health**: Liveness and Readiness probes are available at `/actuator/health/liveness` and `/actuator/health/readiness`.
- **Tracing**: Micrometer/Brave adds `traceId` and `spanId` to all logs. Use tools like Jaeger or Zipkin by forwarding logs via FluentBit/Logstash.

## Disaster Recovery

## Disaster Recovery & Service Mocking

### Mock / Demo Mode Integrations
To simplify local development and deployment in staging/testing environments, certain external dependencies are simulated:
1. **Push Notifications**: Exclusively mocked in `PushGatewayImpl`. Emits `[DEMO PUSH]` logs rather than integrating with an external APNS/FCM provider.
2. **Logistics & Shipping**: Exclusively mocked in `HttpShippingPartnerGateway`. Emits `[DEMO LOGISTICS]` logs on dispatch requests.
3. **Email (greenfield)**: Sends actual emails if configured, but falls back to printing `[DEMO EMAIL]` / `[DEMO OTP EMAIL]` in logs if the SMTP server is local and unauthenticated.

### Redis & Kafka Recovery Expectations
- **Redis Loss**: If Redis cache is cleared or lost, the system degrades gracefully. Primary data remains safe in PostgreSQL/MongoDB. When recovering:
  - Inventory and variant stock values are automatically re-warmed from PostgreSQL upon boot (see `inventoryRedisWarmup` runner).
  - Active flash sales should be pre-warmed manually via the admin portal to re-populate active stock keys before reopening buyer traffic.
- **Kafka / Zookeeper Loss**: If Kafka loses state, outbox events in the relational `order_outbox` table that are still pending or failed will be resent upon application boot, or can be re-triggered manually via the Admin System Dashboard (`/admin/system`).

### Supabase Asset Backup & Restore
If using Supabase storage buckets for product image uploads:
1. **Backup**: Use the Supabase CLI to synchronize storage files locally or to an S3-compatible cold bucket:
   ```bash
   supabase storage pull product-images /backups/supabase/product-images
   ```
2. **Restore**: Synchronize files back into the destination bucket:
   ```bash
   supabase storage push /backups/supabase/product-images product-images
   ```

### Tested Restore Drills
We conduct quarterly restore dry-runs:
1. Spin up an empty target postgres/mongodb environment.
2. Restore the latest daily dump using `pg_restore` and `mongorestore`.
3. Start the application services and execute Flyway migrations to verify schema integrity.
4. Verify smoke accounts can log in and view historical orders.

---

## Environment-Specific Deployment Steps

### Local Development Setup
1. Clone the repository and copy the environment template:
   ```bash
   cp backend/.env.example backend/.env
   cp e-commerce-frontend/.env.example e-commerce-frontend/.env.local
   ```
2. Start infrastructure:
   ```bash
   docker compose up -d
   ```
3. Run backend boot application:
   ```bash
   ./gradlew :bootstrap:bootRun
   ```
4. Start frontend dev server:
   ```bash
   cd e-commerce-frontend && npm run dev
   ```

### Staging & Production Deployment
1. **Database Schema Migrations**: Before rolling out new container images, run Flyway migrations as a pre-deploy hook (or let the app run them on startup if database permissions allow DDL execution).
2. **Deployment Strategy**: Use a Rolling Update or Blue-Green deployment with Kubernetes Deployments to achieve zero-downtime.
3. **Liveness/Readiness Probes**: Configure Kubernetes probes pointing to:
   - Readiness: `/actuator/health/readiness`
   - Liveness: `/actuator/health/liveness`

### Rollback Strategy
If a deployment fails:
1. **DDL Rollbacks**: Relational database migrations are forward-only. If a migration fails, manually correct the schema backward-compatibility, or deploy a forward-fix migration. Do **not** modify or manually delete rows from `flyway_schema_history` directly.
2. **Container Image Rollback**: Revert the Kubernetes deployment image tag to the last stable release:
   ```bash
   kubectl rollout undo deployment/ecommerce-core-bootstrap
   kubectl rollout undo deployment/ecommerce-frontend
   ```

---

## Environment Variable Matrix

| Variable Name | Description | Default / Local Dev | Staging / Production |
| --- | --- | --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/ecommerce` | Required |
| `SPRING_DATA_MONGODB_URI` | MongoDB connection URL | `mongodb://localhost:27017/ecommerce` | Required |
| `SPRING_DATA_REDIS_HOST` | Redis host | `localhost` | Required |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers bootstrap list | `localhost:9092` | Required |
| `JWT_SECRET` | HS256 JWT sign/verify key | `d2VyZXR5dWlvcGFzZGZnaGprbHp4Y3Zibm0xMjM0NTY=` | Strong Random Secret |
| `MAIL_FROM` | Unified email sender address | `no-reply@example.com` | Verified SMTP Sender |
| `MAIL_HOST` | SMTP server host | `localhost` | Production SMTP Relay |
| `MAIL_PORT` | SMTP server port | `1025` | `587` / `465` |

