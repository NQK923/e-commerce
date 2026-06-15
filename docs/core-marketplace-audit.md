# Core Marketplace Completion Audit

Last updated: 2026-06-15

This checklist tracks the finite "production-ready core marketplace" goal for Buyer, Seller, and Admin flows. Status values:

- `[x]` verified or fixed in this audit batch
- `[ ]` open
- `[~]` partially verified, needs stronger runtime/smoke evidence

## Verification Gates

- [x] Backend compile/package gate: `.\gradlew.bat build --console=plain` passed on 2026-06-12. Current caveat: most modules still report `NO-SOURCE`, but targeted regression tests now run for identity/order/notification modules.
- [x] Frontend dependency install completed with `npm install`.
- [x] Frontend lint gate: `npm run lint` passed on 2026-06-12.
- [x] Frontend production build gate: `npm run build` passed on 2026-06-12 after browser-smoke fixes. Build remains dev-safe when Supabase env vars are missing and logs the expected warning.
- [x] Frontend dependency audit gate: `npm audit --audit-level=moderate` passed on 2026-06-12 with 0 vulnerabilities.
- [x] Docker Compose config validates without a `.env` file and includes dev defaults for Postgres, MongoDB, Redis, Kafka, and Mailpit.
- [x] Backend runtime health smoke: Compose dependencies healthy and `:bootstrap:bootRun` reached `/actuator/health` status `UP` on 2026-06-12.
- [x] Runtime backend API smoke test buyer/seller/admin user flows against local services passed on 2026-06-12 using dev seed users and products.
- [x] Browser UI smoke test passed for buyer login, product list/detail, add-to-cart, checkout COD order creation, order detail, seller dashboard/products/orders/promotions/settings, admin dashboard/users/products/orders/flash-sales/sellers/reports/settings, and 390px mobile overflow checks on core pages.
- [x] Dev CORS smoke passed for `Origin: http://127.0.0.1:3000` preflight to `/api/products`.
- [~] Add automated tests for critical backend and frontend flows. Initial backend regression tests now cover CORS dev origins, dev-safe OAuth callback gating, VNPay payment ownership and callback verification, notification mark-read ownership, chat send/delivery authorization, seller product mutation ownership/preservation, seller application review, product report admin actions, admin flash sale mutation validation/cache warm-up, and daily sales report aggregation; broader service/UI regression coverage remains open.

## P0 / Security And Build

- [x] Remove hard-coded startup admin credentials from backend source. Admin seed is now opt-in via `IDENTITY_SEED_ADMIN_ENABLED`, `IDENTITY_SEED_ADMIN_EMAIL`, and `IDENTITY_SEED_ADMIN_PASSWORD`.
- [x] Fix frontend build failure in seller settings auth hook usage.
- [x] Fix frontend lint errors in notification socket and shared API params typing.
- [x] Set `turbopack.root` in Next config to avoid workspace-root inference from unrelated parent lockfiles.
- [x] Resolve `npm audit` findings. Upgraded Next tooling and forced patched transitive PostCSS with npm overrides; `npm audit --audit-level=moderate` now reports 0 vulnerabilities.
- [x] Enforce role boundaries on high-risk backend endpoints. Admin user/application/report endpoints are admin-only, seller product/promotion/order operations require seller/admin authority, and buyer endpoints require authentication.
- [x] Enforce order ownership/role checks. Buyer-created orders now derive `userId` from the authenticated token, buyer order listing is scoped to the authenticated buyer, seller order listing is scoped to the authenticated seller, and order state transitions reject unauthorized roles.
- [x] Enforce payment initiation ownership. VNPay initiation for `/api/orders/{orderId}/payment/vnpay` now requires the authenticated buyer owner or admin; sellers cannot initiate payment on buyer orders.
- [x] Enforce notification ownership. Notification list, unread count, record, and mark-read derive or validate `userId` from the authenticated principal; only admin can target another user, and cross-user mark-read returns 403.
- [x] Return real HTTP error statuses through the shared exception handler instead of serializing errors with HTTP 200.
- [x] Add shared `AccessDeniedDomainException` mapping to HTTP 403 for application-layer ownership violations without coupling application services to Spring Web.
- [x] Fix checkout browser blocker: frontend now serializes checkout address to the backend `CreateOrderCommand.address` string contract instead of sending an object that caused Jackson parse failures and HTTP 500.
- [x] Harden local dev CORS without wildcard origins. Backend now supports comma-separated `FRONTEND_ORIGINS` and defaults to both `http://localhost:3000` and `http://127.0.0.1:3000`.
- [x] Add targeted backend regression tests for payment ownership, notification ownership, and CORS origin handling. Also fixed the Gradle/JUnit runtime by adding `org.junit.platform:junit-platform-launcher`.

## P1 / Core Marketplace Flows

- [x] Auth: backend login smoke passed for seeded buyer, seller, and admin accounts. `/api/auth/me` is reachable for an authenticated buyer. OAuth now has a dev-safe local callback path gated by `IDENTITY_OAUTH2_DEV_CALLBACK_ENABLED` and frontend `NEXT_PUBLIC_OAUTH_DEV_MODE`.
- [x] Buyer browse/search/product detail/cart/checkout: backend API smoke covered product list, product detail, advanced search, cart add/read, promotion apply, and COD order creation. Browser smoke also covered login, product list/detail, add-to-cart, checkout form, COD order creation, and order detail redirect.
- [~] Payment: COD order payment smoke passed and VNPay initiation ownership smoke passed. VNPay callback verification now has regression coverage for signed success, invalid signatures, signed gateway failures, idempotent duplicate callbacks, amount mismatch protection, and a dev-safe signed local return URL. Real external VNPay sandbox/provider return smoke is still open.
- [x] Orders/tracking/returns/refunds: backend API smoke covered create, buyer list, seller list, pay, ship, deliver, return request, and seller return approval to final status `RETURNED`.
- [~] Seller registration/approval/product/order/promotion management: seeded seller is approved; runtime and browser smoke covered seller dashboard, product list, order list, coupon list, promotions page, and settings page. Seller product create/update now has regression coverage for seller ownership, indexing, inventory seed event publication, and preserving existing variants/images on partial updates. Seller application approval/rejection now has regression coverage for role elevation, no role elevation on rejection, idempotent review, and rejecting non-pending status changes. Full seller registration and product create/edit browser smoke remains open.
- [~] Admin dashboard/users/sellers/products/orders/reports/flash sales: runtime and browser smoke covered admin dashboard, user list, seller application list, product list, order list, flash-sale list, reports page, and settings page. Admin flash sale create now has regression coverage for valid persistence, Redis stock warm-up, invalid discount/window/quantity rejection, and promotion domain errors mapping through the shared domain exception path. Product report resolve/reject now has regression coverage and not-found errors use `ProductDomainException` instead of generic runtime errors. Full admin mutation browser smoke remains open.
- [~] Notifications/chat: runtime smoke covered notification record/list/unread/mark-read ownership and chat conversation list. Chat send/delivery now has regression coverage for conversation participant authorization, online STOMP delivery, offline Kafka fan-out, and offline notification event creation. Frontend notification realtime hook now uses the configured API base URL and the backend `/ws/chat` STOMP endpoint instead of a hard-coded localhost SockJS endpoint. `scripts/smoke-websocket.ps1` now provides a repeatable local STOMP smoke for authenticated connect, chat subscriptions, seller ack, and buyer delivery; full browser WebSocket and notification push delivery smoke remain open.
- [x] Reports: runtime smoke covered daily report endpoint availability, and daily sales report aggregation now reads real order rows, excludes non-revenue statuses, persists a daily snapshot, and has regression tests for date/status filtering.
- [x] Promotion usage persistence: `PromotionUsageRepositoryImpl` now writes durable rows to `promotion_usages` through Flyway migration V10.
- [x] Seller coupon listing runtime error fixed by eagerly loading coupon applicable product ids.
- [x] Dev seed workflow: Flyway migrations seed repeatable buyer, seller, approved seller application, smoke products/variants/images/tags, coupon, flash sale, default inventory rows, and opt-in admin account through Compose env defaults.
- [x] Inventory smoke stock path: default inventory seed plus Redis warm-up supports local order reservation for smoke product variants.

## P2 / Operations And Environment

- [x] Supabase config is build-safe when env vars are missing, and upload flows now have a dev-safe local placeholder fallback for smoke tests without real storage secrets. Real Supabase bucket validation remains an external deployment check.
- [~] Kafka, Redis, MongoDB, SMTP, OAuth, and VNPay have placeholder/default config in places; Docker Compose now provides local Kafka, Redis, MongoDB, Postgres, Mailpit, a gated dev OAuth callback, and a gated dev VNPay signed return path. Backend health smoke is `UP`; Elasticsearch search behavior, WebSocket delivery, real VNPay provider return, and full integration flows still need validation.
- [~] Browser UI polish: desktop and 390px smoke did not show blocking layout overflow, but seller/admin pages still mix English and Vietnamese labels and chat widget can show a connection-error state when WebSocket is unavailable.
- [x] Document exact local smoke-test env values and commands once seed workflow is complete. `docs/verification.md` records Docker Compose services, non-Docker backend env values, seed accounts, health checks, backend smoke commands, and frontend route smoke targets.
- [x] Add CI-friendly command set for backend build, frontend lint/build, and smoke tests. `scripts/verify-ci.ps1` runs Docker Compose config validation, backend build/tests, frontend lint, and frontend build; `docs/verification.md` records targeted regression and runtime smoke command sets.

## 2026-06-12 Runtime Smoke Evidence

Backend API smoke returned:

```json
{
  "orderId": "c7890be2-f3be-4cd1-982f-f77553440d06",
  "finalOrderStatus": "RETURNED",
  "unauthUsersAll": 401,
  "buyerAdminDenied": 401,
  "buyerSellerFilterDenied": 403,
  "couponCount": 1,
  "flashSaleCount": 1,
  "adminUserCount": 3,
  "notificationCount": 1,
  "chatConversationCount": 0
}
```

Smoke covered seeded buyer `buyer@example.local`, seller `seller@example.local`, and opt-in admin `admin@example.local` using local services only. Remaining evidence gaps before final completion: broader automated regression tests, frontend browser smoke across core UI routes, real VNPay provider sandbox return, full browser WebSocket and notification push delivery smoke, and seller/admin mutation smoke.

Additional security boundary smoke returned:

```json
{
  "orderId": "3d8f2c35-7479-420a-b6c1-0cbfa7c965b5",
  "sellerVnPayDenied": 403,
  "buyerVnPayAllowed": 200,
  "buyerOwnNotificationCount": 2,
  "buyerUnreadCount": 2,
  "buyerCrossListDenied": 403,
  "buyerCrossRecordDenied": 403,
  "buyerCrossReadDenied": 403,
  "buyerOwnRead": 200
}
```

Browser UI smoke returned these key evidence points:

- Buyer UI: login as `buyer@example.local`, product list/detail rendered seeded products, add-to-cart incremented cart badge, checkout COD order succeeded after address serialization fix, and order detail displayed `PENDING` for order `e55f4f6e-780d-4539-9a0c-7fbd1f59314c`.
- Seller UI: login as `seller@example.local`; dashboard, products, orders, promotions, and settings rendered seeded products/orders/coupon.
- Admin UI: login as `admin@example.local`; dashboard, users, products, orders, flash sales, sellers, reports, and settings rendered seeded data.
- Mobile smoke at 390px found no document-level horizontal overflow on home, products, order detail, or admin dashboard.
- CORS smoke for `http://127.0.0.1:3000` returned `Access-Control-Allow-Origin: http://127.0.0.1:3000`.

Automated regression test evidence added on 2026-06-12:

- `.\gradlew.bat :order:order-adapter:test --console=plain` passed, covering VNPay initiation allowed for buyer owner and rejected for non-owner seller.
- `.\gradlew.bat :notification:notification-application:test --console=plain` passed, covering notification mark-read allowed for owner and rejected for non-owner.
- `.\gradlew.bat :identity:identity-adapter:test --console=plain` passed, covering CORS support for both `localhost:3000` and `127.0.0.1:3000`.
- `.\gradlew.bat build --console=plain` passed after adding the tests.

Automated regression test evidence added on 2026-06-15:

- `.\gradlew.bat :report:report-infrastructure:test --console=plain` passed, covering daily report aggregation from real `orders` rows, date-boundary filtering, exclusion of `PENDING`, `CANCELLED`, and `RETURNED` statuses, zero-result behavior, and daily snapshot upsert wiring.
- `.\gradlew.bat :order:order-application:test --console=plain` passed, covering VNPay callback success transitioning a pending order to paid, transaction success update, inventory confirmation, failed gateway callbacks, payment amount mismatch rejection, idempotent duplicate callbacks, and raw callback parameter forwarding to the gateway verifier.
- `.\gradlew.bat :order:order-infrastructure:test --console=plain` passed, covering VNPAY signed success callback parsing, invalid signature rejection, signed gateway failure parsing, amount conversion, transaction reference mapping, and raw payload capture.
- `.\scripts\verify-ci.ps1` passed, covering the new CI script's Docker Compose config validation, backend Gradle build/tests, frontend lint, and frontend production build command set.
- `.\gradlew.bat :chat:chat-application:test --console=plain` passed, covering chat message send validation, conversation participant authorization, online delivered status handling, offline pending notification fan-out, and new conversation creation.
- `.\gradlew.bat :chat:chat-infrastructure:test --console=plain` passed, covering STOMP delivery to `/queue/chat/messages` and Kafka offline notification events on `chat.offline.message` keyed by receiver id.
- `.\gradlew.bat :product:product-application:test --console=plain` passed, covering seller product create mutation, initial inventory event publication, seller ownership rejection on update, search indexing failure tolerance, required seller id validation, and preserving existing variants/images on partial updates.
- `npm run lint` and `npm run build` passed after adding the frontend upload placeholder fallback for missing Supabase env vars.
- `.\gradlew.bat :identity:identity-adapter:test --console=plain` passed after adding the dev-safe OAuth callback endpoint, covering disabled-by-default behavior and successful enabled local callback response shape for the frontend.
- `npm run lint`, `npm run build`, `.\gradlew.bat build --console=plain`, and `docker compose config --quiet` passed after wiring the frontend OAuth dev mode and Docker Compose local OAuth callback env.
- `.\gradlew.bat :identity:identity-application:test --console=plain` passed, covering seller application approval/rejection behavior, seller role elevation, idempotent review, and non-pending review rejection.
- `.\gradlew.bat :promotion:promotion-application:test --console=plain` passed, covering admin flash sale creation persistence, stock cache warm-up, invalid discount/window/quantity rejection, and flash sale list delegation.
- `.\gradlew.bat build --console=plain` passed after adding seller application and flash sale mutation regression tests and mapping `PromotionDomainException` through the shared domain exception base.
- `.\gradlew.bat :product:product-application:test --console=plain` passed after adding product report admin action regression coverage for report creation/list mapping, resolve/reject status transitions, and domain not-found errors without saving.
- `.\gradlew.bat build --console=plain` passed after switching product report not-found handling from generic runtime errors to `ProductDomainException`.
- `npm run lint` and `npm run build` passed after wiring the frontend notification WebSocket hook to the configured native STOMP endpoint.
- `.\gradlew.bat :order:order-infrastructure:test --console=plain` passed after adding dev-safe VNPay local return URL generation, covering signed callback URL creation and round-trip verification through `VnPayGateway.verify`.
- `.\gradlew.bat build --console=plain` and `docker compose config --quiet` passed after enabling the gated dev VNPay return path in backend configuration and Docker Compose defaults.
- `scripts/smoke-websocket.ps1` was added as a repeatable runtime smoke for seeded buyer/seller STOMP auth, `/user` queue subscriptions, seller chat ack, and buyer chat delivery. It still requires a running local stack, so it is documented as a runtime smoke command rather than part of offline CI.
