# Verification Command Set

Use this page as the repeatable command set for local CI-style checks and runtime smoke testing.

## CI-Friendly Gates

Run from the repository root on Windows PowerShell:

```powershell
.\scripts\verify-ci.ps1
```

What it runs:

```powershell
docker compose config --quiet
.\gradlew.bat build --console=plain
cd e-commerce-frontend
npm run lint
npm run build
npx playwright test
```

Fresh CI agents should install frontend dependencies from the lockfile:

```powershell
.\scripts\verify-ci.ps1 -InstallFrontendDependencies
```

Useful narrow variants:

```powershell
.\scripts\verify-ci.ps1 -SkipFrontend
.\scripts\verify-ci.ps1 -SkipBackend
.\scripts\verify-ci.ps1 -SkipDockerConfig
```

## Targeted Backend Regression Gates

Run from the repository root:

```powershell
.\gradlew.bat :identity:identity-adapter:test --console=plain
.\gradlew.bat :identity:identity-application:test --console=plain
.\gradlew.bat :order:order-adapter:test --console=plain
.\gradlew.bat :order:order-application:test --console=plain
.\gradlew.bat :order:order-infrastructure:test --console=plain
.\gradlew.bat :notification:notification-application:test --console=plain
.\gradlew.bat :promotion:promotion-application:test --console=plain
.\gradlew.bat :product:product-application:test --console=plain
.\gradlew.bat :product:product-infrastructure:test --console=plain
.\gradlew.bat :report:report-infrastructure:test --console=plain
```

These cover the current high-risk regression areas: dev CORS origins, dev-safe OAuth callback gating, seller application review, VNPay ownership and callback verification, notification ownership/realtime delivery, seller product/report mutations, Elasticsearch adapter query/mapping behavior, admin flash sale creation validation/cache warm-up, and daily sales report aggregation.

## Local Runtime Smoke Prerequisites

Run the local backing services and app from the repository root:

```powershell
docker compose up -d redis zookeeper kafka mailpit
```

*Note: PostgreSQL and MongoDB can be run locally via Docker, or hosted on Supabase and MongoDB Atlas respectively. Make sure your `.env` contains the correct URIs. If you need local instances:*

```powershell
docker run -d --name postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=ecommerce -p 5432:5432 postgres:15
docker run -d --name mongo -p 27017:27017 mongo:6
```

If you encounter Flyway checksum errors due to old baseline migrations, you can clear your local database before starting:
```powershell
docker exec -it postgres psql -U postgres -d ecommerce -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

Then start the backend with dev-safe local defaults.

For a non-Docker backend run, use equivalent environment values before `:bootstrap:bootRun` (Note: `JWT_SECRET` is required):

```powershell
$env:JWT_SECRET="YourSuperSecretKeyForJwtAuthenticationMustBe32BytesOrMore!"
$env:DB_URL="jdbc:postgresql://localhost:5432/ecommerce"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:SPRING_REDIS_HOST="localhost"
$env:SPRING_REDIS_PORT="6379"
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:MONGO_URI="mongodb://localhost:27017/ecommerce"
$env:MONGO_DATABASE="ecommerce"
$env:MAIL_HOST="localhost"
$env:MAIL_PORT="1025"
$env:MAIL_SMTP_AUTH="false"
$env:MAIL_SMTP_STARTTLS_ENABLE="false"
$env:MANAGEMENT_HEALTH_ELASTICSEARCH_ENABLED="false"
$env:IDENTITY_SEED_ADMIN_ENABLED="true"
$env:IDENTITY_SEED_ADMIN_EMAIL="admin@example.local"
$env:IDENTITY_SEED_ADMIN_PASSWORD="LocalAdmin123!"
$env:IDENTITY_OAUTH2_DEV_CALLBACK_ENABLED="true"
$env:VNPAY_DEV_RETURN_ENABLED="true"
.\gradlew.bat :bootstrap:bootRun
```

Start the frontend in a second terminal:

```powershell
cd e-commerce-frontend
npm run dev
```

When Supabase credentials are unavailable, the frontend dev server uses a local upload placeholder by default so seller registration, seller product image upload, profile avatar upload, and chat attachment smoke tests can continue without real storage secrets. To make this explicit or to use the same behavior outside `npm run dev`, set:

```powershell
$env:NEXT_PUBLIC_UPLOAD_FALLBACK_MODE="placeholder"
```

This returns `/upload-placeholder.svg?...` for new uploaded files only when `NEXT_PUBLIC_SUPABASE_URL` and `NEXT_PUBLIC_SUPABASE_ANON_KEY` are missing. Set `NEXT_PUBLIC_UPLOAD_FALLBACK_MODE="disabled"` and provide the Supabase URL, anon key, and bucket names to smoke real bucket uploads.

For local OAuth smoke tests without Google/Facebook credentials, keep `IDENTITY_OAUTH2_DEV_CALLBACK_ENABLED=true` on the backend and enable the frontend stub redirect:

```powershell
$env:NEXT_PUBLIC_OAUTH_DEV_MODE="enabled"
```

With that flag, the login page's Google/Facebook buttons route through `/oauth2/callback` using deterministic local provider ids instead of leaving the app for an external provider. Leave both flags disabled outside local/dev smoke testing.

## Runtime Smoke Accounts

If not already seeded, you can run `backend/bootstrap/src/main/resources/db/seed/dev_seeds.sql` against your local database to create these local smoke users:

| Role | Email | Password |
| --- | --- | --- |
| Buyer | `buyer@example.local` | `Buyer@123` |
| Seller | `seller@example.local` | `Seller@123` |
| Admin | `admin@example.local` | `LocalAdmin123!` |

Do not use these as production credentials.

*Note: The `dev_seeds.sql` script pre-seeds a smoke product (`prod-smoke-1`) and an active flash sale (`00000000-0000-0000-0000-000000000f51`) so you do not need to create them manually for basic smoke testing.*

## Runtime Smoke Commands

Health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Buyer login:

```powershell
$buyer = Invoke-RestMethod http://localhost:8080/api/auth/login `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"buyer@example.local","password":"Buyer@123"}'
$buyerHeaders = @{ Authorization = "Bearer $($buyer.accessToken)" }
Invoke-RestMethod http://localhost:8080/api/auth/me -Headers $buyerHeaders
```

Seller and admin login:

```powershell
$seller = Invoke-RestMethod http://localhost:8080/api/auth/login `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"seller@example.local","password":"Seller@123"}'
$sellerHeaders = @{ Authorization = "Bearer $($seller.accessToken)" }

$admin = Invoke-RestMethod http://localhost:8080/api/auth/login `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"admin@example.local","password":"LocalAdmin123!"}'
$adminHeaders = @{ Authorization = "Bearer $($admin.accessToken)" }
```

Core read-path checks:

```powershell
Invoke-RestMethod "http://localhost:8080/api/products?page=0&size=10"
Invoke-RestMethod "http://localhost:8080/api/products?search=smoke&page=0&size=10"
Invoke-RestMethod "http://localhost:8080/api/coupons" -Headers $buyerHeaders
Invoke-RestMethod "http://localhost:8080/api/flash-sales"
Invoke-RestMethod "http://localhost:8080/api/orders?page=0&size=10" -Headers $buyerHeaders
Invoke-RestMethod "http://localhost:8080/api/orders?sellerId=00000000-0000-0000-0000-000000000102&page=0&size=10" -Headers $sellerHeaders
Invoke-RestMethod "http://localhost:8080/api/users/all?page=0&size=10" -Headers $adminHeaders
Invoke-RestMethod "http://localhost:8080/api/reports/daily?date=$((Get-Date).ToString('yyyy-MM-dd'))" -Headers $adminHeaders
```

Dev-safe OAuth callback:

```powershell
Invoke-RestMethod http://localhost:8080/api/auth/oauth2/callback `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"provider":"google","providerUserId":"local-google-buyer","email":"local.google@example.local","name":"Local Google Buyer"}'
```

Dev-safe VNPay return:

When `VNPAY_DEV_RETURN_ENABLED=true`, `/api/orders/{orderId}/payment/vnpay` returns a signed frontend return URL instead of redirecting to the external VNPay sandbox. Open the returned `paymentUrl`; the frontend `/payment/vnpay-return` page will call `/api/payments/vnpay/return` with the signed parameters and complete the local payment callback path. Leave the flag disabled for real VNPay sandbox testing.

Frontend route smoke:

```text
http://localhost:3000/login
http://localhost:3000/products
http://localhost:3000/cart
http://localhost:3000/seller/dashboard
http://localhost:3000/seller/products
http://localhost:3000/seller/orders
http://localhost:3000/admin
http://localhost:3000/admin/users
http://localhost:3000/admin/products
http://localhost:3000/admin/orders
http://localhost:3000/admin/reports
```

Realtime WebSocket smoke uses the backend STOMP endpoint at `/ws/chat` for both chat and user notification queues. The frontend derives this from `NEXT_PUBLIC_API_BASE_URL`, so non-local API base URLs should not require code changes.

Repeatable local STOMP smoke:

```powershell
.\scripts\smoke-websocket.ps1
```

This logs in the seeded buyer and seller, connects both clients to `/ws/chat` with JWT-backed STOMP `CONNECT`, subscribes to `/user/queue/chat/messages`, `/user/queue/chat/ack`, and `/user/queue/notifications`, sends a seller-to-buyer chat message through `/app/chat.send`, verifies both the seller ack and buyer delivery frames, records a buyer notification through `/api/notifications/record`, and verifies the buyer notification delivery frame. It requires the backend and local backing services to be running.

Stop local services when finished:

```powershell
docker compose stop
```
