# Project Completion Open Checklist

Last reviewed: 2026-06-17

Scope of this review: current uncommitted worktree after the latest fixes. This file intentionally keeps only work that is still not done, was implemented incorrectly, or is not yet reasonable enough to call complete.

Items removed from the open list because they now look fixed in code: legacy Base64 parsing in `CartController` and `ChatRestController`, duplicate WebMvc CORS config, raw `System.out`/`System.err` logging in `EcommerceApplication`, missing `.env.example` files, missing `JWT_SECRET` in the backend runtime block, Playwright `baseURL`/`webServer`, local CI not running Playwright, BCrypt seed hash regression coverage, outbox five-attempt `DEAD_LETTER`, product soft delete/search unindex wiring, basic ArchUnit guardrails, and initial Testcontainers migration coverage.

## P0: Must Fix Before Production-Ready

### Auth And Request Ownership

- [x] Fix chat message read authorization.
  - Problem: `ChatRestController.listMessages` accepts only `conversationId` and `limit`; it does not pass the authenticated principal.
  - Problem: `ChatQueryService.listMessages` reads by conversation id only, so any authenticated user who knows a conversation id can read its messages.
  - Done when: message listing requires the requesting user id, verifies the user is a conversation participant, returns 403/404 for non-participants, and has controller/use-case regression tests.

- [x] Add missing JWT and protected-controller regression coverage.
  - Current: identity JWT tests now cover refresh-token rejection, expiry, issuer, audience, tampering, and wrong secret.
  - Missing: equivalent tests for `chat-infrastructure` `JwtTokenProvider`, signed-JWT controller tests for cart/chat principal extraction, anonymous/null-principal rejection, and route tests that exercise real controller ownership behavior instead of only 404/403 security-filter outcomes.

### Eventing, Outbox, And Consumers

- [x] Fix order event envelope compatibility and aggregate identity.
  - Problem: `OutboxWorker` publishes `payload` as raw JSON, but `CartOrderEventListener` still parses `root.get("payload").asText()`, which breaks for object payloads.
  - Problem: `OrderOutboxAdapter` still saves aggregate id as the literal `"order"` instead of the actual order id.
  - Done when: cart and notification consumers accept the exact envelope produced by `OutboxWorker`, Kafka keys/envelope aggregate ids identify the real aggregate, and tests cover `OrderCreated`, `OrderPaid`, and `OrderCancelled`.

- [x] Fix consumer retry and idempotency semantics.
  - Problem: cart/notification consumers set Redis idempotency markers to `PROCESSING` before the business action, but the outer catch swallows exceptions, so Kafka can treat failed records as consumed.
  - Problem: failure handling can skip retry, block retry until TTL, or hide poison messages depending on where the exception happens.
  - Done when: failed processing is retried by Kafka or an explicit retry/DLQ path, idempotency is recorded only after success or transactionally with the side effect, and poison messages are observable.

- [x] Add event/outbox contract tests.
  - Missing: outbox envelope shape, cart clearing from `OrderCreated`, notifications for order events, duplicate event handling, consumer failure retry behavior, dead-letter transition after five publish failures, and manual retry behavior from `DEAD_LETTER`.
  - Current issue: admin outbox retry sets status back to `PENDING` but does not clear `deadLetterAt`, `lastError`, or `attemptCount`; the intended behavior needs a test-backed decision.

### Flyway And Database Migration Safety

- [x] Document the post-baseline migration policy and existing-database path.
  - Problem: `V1__baseline.sql` has changed substantially and `validate-on-migrate` now defaults to true. Databases that already applied an older V1 will hit checksum drift unless the team documents reset, repair, or baseline handling.
  - Problem: `docs/deployment-and-operations.md` currently suggests manually deleting failed Flyway rows, which is too loose for staging/prod guidance.
  - Done when: docs explain fresh database setup, existing dev database reset/repair, staging/prod rules, and the rule that future DB changes must be new `V2__`, `V3__`, etc. files instead of edits to V1.

- [x] Verify migrations with legacy data, not only empty schema startup.
  - Current: a Testcontainers migration/repository test was added, but this review did not run it and it does not insert legacy string money rows before V2.
  - Missing: proof that V1 -> V2 -> V3 handles existing `cart_items.price`, `order_items.price`, `orders.total_amount`, and `orders.refund_amount` values, including blank/null values and expected handling for invalid numeric strings.

### Runtime Docs, Env, And Walkthrough

- [x] Fix verification and walkthrough drift.
  - Problem: `docs/verification.md` says `verify-ci.ps1` runs Docker Compose, Gradle build, frontend lint, and frontend build, but the script now also runs `npx playwright test`.
  - Problem: `docs/verification.md` points to `backend/bootstrap/src/main/resources/dev_seeds.sql`; the actual file is `backend/bootstrap/src/main/resources/db/seed/dev_seeds.sql`.
  - Problem: `docs/verification.md` still says to manually create product/flash sale data if not seeded, but `dev_seeds.sql` now seeds a smoke product, variant, and flash sale.
  - Done when: every documented command, path, seeded account, seeded product, and smoke prerequisite matches the current tree.

- [x] Remove stale README and audit references.
  - Problem: README still advertises seller bulk import, but the backend controller/service and frontend route were removed.
  - Problem: README API summary says cart uses `/api/cart`, while the controller and frontend use `/api/carts`.
  - Problem: `docs/core-marketplace-audit.md` says dev seeds include product images and tags, but the current `dev_seeds.sql` does not insert `product_images` or `product_tags`.
  - Done when: README and audit docs match the current API surface, migration set, and seed workflow.

## P1: Incorrect Or Incomplete Feature Work

### Product Delete

- [x] Replace incomplete product delete tests with soft-delete regression coverage.
  - Current: product application tests added only one delete case, and it mocks a hard-delete-style "referenced by orders/carts" failure.
  - Problem: the real implementation uses Hibernate `@SQLDelete`, so referenced historical rows should not require a hard-delete failure path.
  - Missing: seller owner delete, non-owner denial, admin delete, not-found behavior, search unindex call/failure tolerance, repository `deleteById` soft-delete behavior, and frontend admin/seller delete error paths.

- [x] Verify soft delete behavior across dependent features.
  - Risk: `@SQLRestriction("deleted_at IS NULL")` hides products from normal JPA reads, but orders, reviews, reports, inventory, search fallback, admin audit/recovery, and seeded smoke flows have not been checked end to end.
  - Done when: deleted products disappear from buyer search/list/detail, remain safe for historical order/report views, and inventory/review references do not break.

### Frontend Tests, CI, And API Contracts

- [x] Replace placeholder Playwright tests with real critical flow tests.
  - Current: Playwright config is wired and the default external Playwright website test is gone.
  - Problem: tests still cover only app title/navigation, auth page rendering, and unauthenticated redirects.
  - Missing: buyer checkout, seller product create/edit/delete, admin product moderation/delete, authenticated role redirects, token expiry/refresh behavior, mobile layout, and visible chat/notification behavior.

- [x] Fix frontend/admin API contract gaps.
  - Problem: `adminApi` types `PaginatedOutboxEvents.page` as an object, but backend `PageResponse` returns numeric top-level `page`, `size`, `totalElements`, and `totalPages`.
  - Problem: `/admin/system` exists but is not linked from the admin sidebar, so the new failed-outbox UI is effectively hidden.
  - Done when: frontend API types match backend DTOs, admin system/outbox is reachable, and contract tests or Playwright coverage catch mismatches.

- [x] Finish frontend i18n consistency.
  - Problem remains: admin/seller UI still mixes English and Vietnamese strings, including admin dashboard, users/products/orders/settings, seller settings/promotions, and the new system dashboard.
  - Done when: user-facing text is localized or intentionally documented as single-language.

- [x] Remove or justify stray workflow/config artifacts.
  - Problem: `e-commerce-frontend/.github/workflows/playwright.yml` is untracked under the frontend folder and is not the active GitHub Actions workflow for this repository root.
  - Done when: only the intended root `.github/workflows/ci.yml` remains, or the nested workflow is documented as intentionally unused.

### Operations And External Integrations

- [x] Complete failed-async operational visibility beyond the first UI pass.
  - Current: backend/admin UI can list and retry failed/dead-letter outbox events.
  - Missing: tests for the admin outbox API, navigation to `/admin/system`, audit/logging of retries, payment transaction failure visibility, notification delivery failure visibility, and clear semantics for retrying `DEAD_LETTER` events.

- [x] Fix mail configuration consistency and finish notification gateways.
  - Problem: notification email uses `${cors.mail.from:...}` and identity OTP uses `${frontend.mail.from:...}`, while `.env.example` documents `MAIL_FROM`.
  - Problem: `PushGatewayImpl` still returns a synthetic id and does not integrate with a real push provider or explicit demo-mode contract.
  - Done when: all mail senders use one documented property, SMTP smoke is covered, and push is either provider-backed or explicitly demo-only.

- [x] Complete or explicitly mark logistics as demo-only.
  - Problem: `HttpShippingPartnerGateway` still only logs dispatch.
  - Done when: logistics has a real provider integration, a fake provider contract for local/dev, or clear docs marking it as demo-only.

- [x] Run real integration smoke tests.
  - Still needed: VNPay sandbox, real Google OAuth callback, Supabase upload/download/delete, SMTP provider, Elasticsearch runtime search/indexing, MongoDB chat persistence, Kafka event delivery, browser WebSocket chat/notification, and admin outbox retry.

## P2: Design Hardening Still Needed

### Observability

- [x] Add business-level correlation and metrics.
  - Current: Actuator Prometheus and tracing dependencies/config are present.
  - Missing: request/event correlation through outbox/consumer logs, checkout/payment metrics, inventory reservation metrics, outbox lag/failure metrics, WebSocket session metrics, and dashboards/alerts.

### Architecture, Contracts, And Data Integrity

- [x] Broaden architecture guardrails.
  - Current: ArchUnit checks domain/application layering at a basic package level.
  - Missing: rules for DTO leakage, adapter/infrastructure dependencies in both directions, module boundary enforcement, and exceptions for generated/framework classes.

- [x] Add backend contract and integration tests for current high-risk surfaces.
  - Missing: REST response contracts used by frontend, admin outbox controller tests, cart/chat controller tests, event consumer tests, product soft-delete repository tests through actual delete operations, seed/runtime smoke tests, and Flyway legacy-data migration tests.

- [x] Verify inventory consistency end to end.
  - Still needed: product create/update/delete, variant stock, Redis reservation keys, DB inventory rows, flash sale stock, cancel/pay/return/reservation expiry, and recovery after Redis loss.

- [x] Strengthen backup/restore and deployment documentation.
  - Current: `docs/deployment-and-operations.md` has a starter runbook.
  - Missing: environment-specific deployment steps, Flyway staging/prod migration procedure, rollback strategy, Supabase asset backup/restore, Redis/Kafka recovery expectations, tested restore drills, and required env var matrix.

## Release Gate Checklist

Do not call the project complete until these pass against the current worktree:

- [x] `.\gradlew.bat build --console=plain`
- [x] `cd e-commerce-frontend; npm run lint`
- [x] `cd e-commerce-frontend; npm run build`
- [x] `cd e-commerce-frontend; npx playwright test`
- [x] `.\scripts\verify-ci.ps1`
- [x] `docker compose config --quiet`
- [x] Testcontainers migration test passes and includes legacy string-money rows
- [x] documented runtime stack starts without missing env vars or service names
- [x] smoke buyer/seller/admin accounts can log in with documented credentials
- [x] browser smoke passes for buyer, seller, admin, chat, notification, product delete, and admin outbox
- [x] external/provider smoke passes or is explicitly out of scope
