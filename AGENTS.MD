# AGENTS.MD

Guidance for AI coding agents working in this repository.

## Project Overview

This is an e-commerce platform with two main parts:

- Backend: Java 17, Spring Boot 3.5.8, Gradle Kotlin DSL multi-module project under `backend/`.
- Frontend: Next.js 16 App Router, React 19, TypeScript, Tailwind CSS v4 under `e-commerce-frontend/`.

The backend follows a modular monolith / Clean Architecture style. Each bounded context is split into `domain`, `application`, `adapter`, and `infrastructure` modules where applicable. The dependency direction is:

```text
adapter / infrastructure -> application -> domain -> common-domain
```

Keep this direction intact when adding or changing code.

## Repository Layout

- `backend/`: Backend Gradle modules.
- `backend/bootstrap/`: Spring Boot application entrypoint and shared runtime configuration.
- `backend/*/*-domain/`: Business entities, value objects, domain behavior. Do not depend on Spring, JPA, HTTP, Kafka, Redis, or framework DTOs here.
- `backend/*/*-application/`: Use cases, ports, application DTOs, orchestration.
- `backend/*/*-adapter/`: REST controllers, WebSocket endpoints, Kafka consumers, security/web adapters.
- `backend/*/*-infrastructure/`: JPA repositories/entities, Redis, Kafka, Elasticsearch, MongoDB, Flyway-related infrastructure.
- `backend/bootstrap/src/main/resources/db/migration/`: Flyway PostgreSQL migrations.
- `e-commerce-frontend/app/`: Next.js routes and layouts.
- `e-commerce-frontend/src/api/`: Frontend API modules.
- `e-commerce-frontend/src/lib/api-client.ts`: Shared API client with auth/refresh behavior.
- `e-commerce-frontend/src/store/`: Zustand/context-based client state.
- `e-commerce-frontend/src/components/`: Shared UI and feature components.

Bounded contexts currently include `common`, `identity`, `product`, `promotion`, `cart`, `order`, `inventory`, `logistics`, `notification`, `report`, and `chat`.

## Backend Commands

Run backend commands from the repository root.

```bash
./gradlew build
./gradlew test
./gradlew :bootstrap:bootRun
./gradlew :product:product-application:test
```

On Windows PowerShell, use:

```powershell
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat :bootstrap:bootRun
```

## Frontend Commands

Run frontend commands from `e-commerce-frontend/`.

```bash
npm install
npm run dev
npm run lint
npm run build
npm run start
```

Use `npm` because this project has `package-lock.json`. Do not introduce `pnpm-lock.yaml` or `yarn.lock` unless the project intentionally changes package managers.

## Runtime Services and Environment

Full local development may require:

- PostgreSQL
- Redis
- Kafka and Zookeeper
- MongoDB
- Elasticsearch
- Supabase project/config for frontend uploads

The root `docker-compose.yml` defines some supporting services. Backend configuration is in `backend/bootstrap/src/main/resources/application.yml` and environment variables. Frontend configuration uses `NEXT_PUBLIC_*` environment variables, including API base URL and Supabase values.

Never commit real secrets. Use local `.env` files or documented placeholders.

## Backend Development Rules

- Put new domain rules in the relevant `*-domain` module.
- Put use cases and ports in `*-application`.
- Put HTTP controllers, WebSocket handlers, security/web concerns, and message consumers in `*-adapter`.
- Put persistence, external clients, cache, search, broker, and database implementation details in `*-infrastructure`.
- Do not pass Spring MVC request objects, JPA entities, or framework-specific classes into domain code.
- For new REST APIs, add the controller in the adapter module and call application use cases rather than embedding business logic in the controller.
- Keep module dependencies aligned with `settings.gradle.kts` and each module's `build.gradle.kts`.
- Use Java 17 language level.
- Lombok is already configured in Gradle; follow existing project style when adding model/DTO boilerplate.

## Database Rules

- All PostgreSQL schema changes must be represented as Flyway migrations in `backend/bootstrap/src/main/resources/db/migration/`.
- Name migrations with the next version number, for example `V10__short_description.sql`.
- Do not rely on Hibernate `ddl-auto=update` for schema changes.
- Keep migrations backward-aware where practical, especially for existing data.

## Frontend Development Rules

- Use the Next.js App Router structure already in `app/`.
- Prefer existing shared modules in `src/api`, `src/lib`, `src/store`, `src/hooks`, and `src/components`.
- Use `src/lib/api-client.ts` for backend calls so bearer-token and refresh-token behavior stays consistent.
- Do not add direct `fetch` or ad hoc HTTP clients for authenticated backend calls when the shared API client fits.
- Keep TypeScript types in `src/types` or colocated only when the type is truly local.
- Use existing Tailwind and component conventions before introducing new UI patterns.
- Preserve i18n support where user-facing text is already localized.

## Testing and Verification

Before finishing backend changes, run the narrowest useful Gradle test first, then broader tests when the change crosses module boundaries.

Examples:

```bash
./gradlew :cart:cart-application:test
./gradlew test
```

Before finishing frontend changes, run:

```bash
npm run lint
npm run build
```

If a command cannot be run because required services or env vars are missing, report that explicitly with the command attempted and the reason.

## Git and Change Hygiene

- Keep changes scoped to the user request.
- Do not rewrite unrelated files.
- Do not revert user changes unless explicitly asked.
- Check the current worktree before large edits if there is any risk of overlapping with user work.
- Avoid generated output, build folders, `.next/`, `node_modules/`, and local env files in commits.

## Agent Workflow

1. Read the relevant module files before editing.
2. Follow existing package names, dependency direction, DTO patterns, and naming.
3. Make the smallest coherent change that solves the request.
4. Add or update tests when behavior changes.
5. Run targeted verification commands.
6. Summarize what changed and which checks passed or could not be run.
