# LankaTrails Backend

[![CI](https://github.com/LankaTrails/lankatrails-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/LankaTrails/lankatrails-backend/actions/workflows/ci.yml)

LankaTrails is a travel platform for Sri Lanka. Tourists plan trips, discover places and book local
service providers; providers manage their listings and bookings; administrators verify providers and
handle complaints. This repository holds the Spring Boot API that serves all three apps, and the
Docker Compose stack that runs the whole system locally.

## The LankaTrails system

| Repository | What it is | Stack |
|---|---|---|
| **lankatrails-backend** (this repo) | REST + WebSocket API, dev Docker stack | Spring Boot 3.5, Java 21 |
| [lankatrails-mobile-app](https://github.com/LankaTrails/lankatrails-mobile-app) | Tourist app: explore, trips, bookings, chat | Expo SDK 58, React Native |
| [lankatrails-provider-dashboard](https://github.com/LankaTrails/lankatrails-provider-dashboard) | Provider web app: services, bookings, payouts | React, Vite, TypeScript |
| [lankatrails-admin-dashboard](https://github.com/LankaTrails/lankatrails-admin-dashboard) | Admin web app: verification, complaints, analytics | React, Vite, TypeScript |

```
 Mobile app (Expo)      Provider dashboard      Admin dashboard
        │                      │                       │
        └──────── REST /api  + STOMP over /ws ─────────┘
                               │
                     Spring Boot backend ── Stripe (payments, Connect payouts)
                               │
     PostgreSQL + PostGIS   MongoDB      Redis                 RabbitMQ
     (core data, geo)       (chat)       (refresh tokens,      (STOMP broker relay)
                                          login throttling)
```

## Features

- **Auth:** email/password sign-up with email verification, JWT access tokens with rotating refresh
  tokens in Redis, role-based access (tourist, provider, admin), login throttling per IP and account.
- **Services:** providers list accommodation, tour guides, transport, activities and food & beverage,
  with images, policies and pricing; tourists search them by city or by distance (PostGIS).
- **Trips:** trip planning with group members and invitations, day-by-day items, budgets and expenses.
- **Bookings and payments:** availability checks, bookings, Stripe PaymentIntents with a platform
  commission, Stripe Connect onboarding for provider payouts, and a verified webhook.
- **Chat:** real-time messaging between tourists and providers over STOMP (RabbitMQ relay), stored in
  MongoDB, with read receipts and file attachments.
- **Trust and safety:** provider licence review and approval, reviews and ratings, tourist complaints
  with admin resolution and Stripe refunds.
- **Insights:** booking and revenue analytics for providers and admins.

## Run the whole system with Docker

Everything runs in containers; you only need Docker (Docker Desktop on macOS/Windows) and Git.

### 1. Clone the repositories side by side

```bash
mkdir lankatrails && cd lankatrails
git clone https://github.com/LankaTrails/lankatrails-backend.git
git clone https://github.com/LankaTrails/lankatrails-admin-dashboard.git
git clone https://github.com/LankaTrails/lankatrails-provider-dashboard.git
git clone https://github.com/LankaTrails/lankatrails-mobile-app.git    # optional, see below
```

The Compose file expects the dashboards next to this repo (`../lankatrails-*-dashboard`). To use
other locations, set `ADMIN_DASHBOARD_DIR`, `PROVIDER_DASHBOARD_DIR` and `MOBILE_APP_DIR`.

### 2. Create the environment files

```bash
cd lankatrails-backend
cp .env.example .env
cp ../lankatrails-admin-dashboard/.env.example ../lankatrails-admin-dashboard/.env
cp ../lankatrails-provider-dashboard/.env.example ../lankatrails-provider-dashboard/.env
```

In `lankatrails-backend/.env`, set at least `JWT_SECRET` (`openssl rand -base64 64`),
`ADMIN_PASSWORD` and `DEMO_USER_PASSWORD`. The database, Redis and RabbitMQ defaults already point at
the Compose services. Email, Stripe and Google keys are optional for a first run; see
[Environment variables](#environment-variables).

### 3. Start the stack

```bash
docker compose -f docker-compose.dev.yml up --build -d
docker compose -f docker-compose.dev.yml ps        # wait until backend shows (healthy)
```

| Service | URL |
|---|---|
| Backend API | http://localhost:8080/api |
| Health check | http://localhost:8080/actuator/health |
| Admin dashboard | http://localhost:5173 |
| Provider dashboard | http://localhost:5174 |
| RabbitMQ management | http://localhost:15672 |

Java changes recompile and restart the backend automatically; the dashboards use Vite hot reload.

### 4. Load the demo data

Once the backend is healthy (it creates the schema on first start):

```bash
./docker/seed/load-seed.sh
```

The script is safe to re-run. It copies placeholder images into `uploads/` and adds:

| Account | Role | Password | Use it in |
|---|---|---|---|
| `ADMIN_EMAIL` from `.env` | Admin | `ADMIN_PASSWORD` | Admin dashboard |
| `olanka@lankatrails.dev` | Provider (Olanka Travels, 18 services) | `DEMO_USER_PASSWORD` | Provider dashboard |
| `tourist@lankatrails.dev` | Tourist with trips, bookings, reviews and complaints | `DEMO_USER_PASSWORD` | Mobile app |
| `tourist2@lankatrails.dev` | Tourist | `DEMO_USER_PASSWORD` | Mobile app |

The admin account is created on first start from `ADMIN_EMAIL` / `ADMIN_PASSWORD`.

### 5. Run the mobile app (optional)

The Expo dev server is an opt-in Compose service. Open the app in Expo Go (SDK 58) on a phone on
the same network; replace the IP with your machine's LAN IP:

```bash
MOBILE_PACKAGER_HOST=192.168.1.20 MOBILE_METRO_BIND=0.0.0.0 \
  docker compose -f docker-compose.dev.yml --profile mobile run --rm --service-ports mobile-app
```

Set up `lankatrails-mobile-app/.env` first; the
[mobile README](https://github.com/LankaTrails/lankatrails-mobile-app#readme) has the details.

### Everyday commands

```bash
docker compose -f docker-compose.dev.yml logs -f backend     # follow backend logs
docker compose -f docker-compose.dev.yml stop                # stop, keep data
docker compose -f docker-compose.dev.yml down -v             # remove containers and all data
```

## Environment variables

All variables live in `.env` (git-ignored); `.env.example` documents each one. Never commit real keys.

| Variables | Purpose | Needed for |
|---|---|---|
| `JWT_SECRET` | Signing key, Base64, at least 256 bits | **Required** |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Admin account created on first start | **Required** |
| `DEMO_USER_PASSWORD` | Password for the seeded demo accounts | Demo data |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL + PostGIS | Defaults work in Compose |
| `MONGODB_URI` | MongoDB (chat history) | Defaults work in Compose |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `REDIS_SSL_ENABLED` | Redis | Defaults work in Compose |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_STOMP_PORT`, `RABBITMQ_VIRTUAL_HOST`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | RabbitMQ AMQP and STOMP relay | Defaults work in Compose |
| `API_PORT`, `POSTGRES_HOST_PORT`, `MONGODB_HOST_PORT`, `REDIS_HOST_PORT` | Ports published on your machine | Only if they clash |
| `JWT_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS` | Token lifetimes | Optional |
| `APP_PUBLIC_API_URL` | Base URL in verification emails | Email |
| `CORS_ALLOWED_ORIGINS` | Browser origins for REST and `/ws` | Defaults cover local apps |
| `LOGIN_RATE_LIMIT_PER_ACCOUNT`, `LOGIN_RATE_LIMIT_PER_IP`, `FORWARD_HEADERS_STRATEGY` | Login throttling, proxy headers | Optional |
| `UPLOAD_ROOT_PATH` | Upload folder inside the container | Optional |
| `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | SMTP (Gmail app password) | Verification and complaint emails |
| `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, `STRIPE_CURRENCY`, `STRIPE_COMMISSION_RATE`, `STRIPE_CONNECT_COUNTRY`, `APP_PROVIDER_DASHBOARD_URL` | Stripe test mode, Connect payouts | Payments |
| `GOOGLE_MAPS_API_KEY` | Server-side travel times | Trip routing |

## Tests

```bash
./mvnw test                  # unit, security and API contract tests (no databases needed)
./mvnw test -Pintegration    # also tests that need PostgreSQL, Redis, MongoDB and RabbitMQ
```

The integration profile expects the data stores at the hosts in `.env`, so run it inside the stack:
`docker compose -f docker-compose.dev.yml exec backend mvn -B test -Pintegration`.

To run them without a local JDK:

```bash
docker run --rm -v "$PWD":/app -w /app maven:3.9.9-eclipse-temurin-21 mvn -B test
```

GitHub Actions runs `./mvnw test` on every push and pull request.

## Project layout

```
src/main/java/com/lankatrails/lankatrails_backend/
  controller/   REST and STOMP endpoints
  service/      business logic (impl/ holds the implementations)
  repositories/ Spring Data JPA and MongoDB repositories
  model/        JPA entities and enums
  dtos/         request and response objects
  security/     JWT, filters, login throttling
  rabbit/       STOMP authentication and broker setup
  config/       web, security, WebSocket, Stripe and startup configuration
docker/         dev entrypoint and demo seed
```

## Screenshots

<!-- Screenshots and GIFs of the system go here. -->

_Coming soon._

## License

[MIT](LICENSE)
