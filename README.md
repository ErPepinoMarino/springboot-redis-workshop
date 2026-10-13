# springboot-redis-workshop

**Taller didáctico de Spring Boot + Redis.** Una colección de mini-módulos independientes (caché, rate limiting, leaderboards, colas, pub/sub, locks...) para aprender ambas tecnologías construyendo, probando y trasteando desde un frontend.

> **Objetivo:** aprender Spring Boot y Redis. El proyecto es la excusa; entender cada línea es el resultado.

## Módulos

Cada módulo demuestra un patrón y tiene su propia API, sus tests y su página en el frontend.

| Módulo | Patrón de Redis | Qué enseña |
|---|---|---|
| **Caché** | *cache-aside* sobre Postgres | `@Cacheable`, TTL, invalidación, *hit ratio* |
| **Rate limiting** | contadores + Lua | atomicidad, filtros, `429` |
| **Leaderboard** | *sorted sets* | rankings en tiempo real |
| **Sesiones** | hashes + TTL | login, expiración |
| **Cola de trabajos** | Streams + consumer groups | trabajo en background, ack, rescate |
| **Pub/Sub** | canales + SSE | eventos en vivo (reloj) |
| **Lock distribuido** | `SET NX PX` | exclusión mutua entre procesos |

## Estado actual

Proyecto **en construcción**, full-stack por módulo. Cerrados: **Módulos 0–6** (fundación, caché, rate limiting, leaderboard, sesiones, cola de trabajos, pub/sub).

- Backend Spring Boot con `GET /healthz`, **Postgres** (JPA + Flyway) y **Redis** cableados; Actuator (`/actuator/health`, `/actuator/metrics`).
- **Módulo 1 — Caché**: `GET`/`DELETE /cache/products/{id}`, `?cached=false`, `GET /cache/products/bench` (benchmark de concurrencia), *cache-aside* con Redis y TTL 60 s.
- **Módulo 2 — Rate limiting**: **token bucket** atómico en **Redis + Lua**; `GET /ratelimit/ping` limitado por IP (`429` + `Retry-After` + `X-RateLimit-*`) y `GET /ratelimit/simulate` (modelo: pasa/cola/descartadas). **Visualización en vivo (SSE)**: `POST /ratelimit/simulate/stream` + `GET /ratelimit/stream` (ticks cada 0.25 s con las atendidas sin/con bucket).
- **Módulo 3 — Leaderboard**: **test de carga** con *sorted sets* de Redis (`ZINCRBY`) vs Postgres (`UPDATE` en transacción): `POST /leaderboard/benchmark` mide las **operaciones completadas por segundo** bajo concurrencia (Redis escala; Postgres se techa en su pool).
- **Módulo 4 — Sesiones**: login con la sesión en **Redis** (*hash* + **TTL**, token en cookie `HttpOnly`); `POST /sessions/login`, `GET /sessions/me`, `POST /sessions/logout`; contadores con **acceso por rol** (`401`/`403`). Contraseñas con **BCrypt**.
- **Módulo 5 — Cola de trabajos**: *job queue* con **Redis Streams** + **consumer group** (`XREADGROUP`/`XACK`); `POST /jobs` (encola y responde al instante) y `GET /jobs/{id}` (estado); **3 workers** procesan con un **handler** por tipo; **reclaim** (`XPENDING` + `XCLAIM`) rescata los huérfanos; estados `PENDING`/`PROCESSING`/`ORPHANED`/`DONE`/`FAILED`.
- **Módulo 6 — Pub/Sub**: **reloj en vivo** con **canales** de Redis (`PUBLISH`/`SUBSCRIBE`) + **SSE** (`text/event-stream`); `GET /clock` (preguntar la hora) y `GET /clock/stream` (el servidor la empuja cada 0.5 s). `SseRegistry` **por topic** con *fan-out* y limpieza al desconectar. Página `/pubsub` (dos paneles: preguntar vs empujar). Integra además el **rate limiting en vivo** (Módulo 2): `POST /ratelimit/simulate/stream` + `GET /ratelimit/stream` → **gráfico de líneas** (sin/con bucket).
- Frontend (**Next.js**): páginas `/cache`, `/ratelimit`, `/leaderboard`, `/sessions`, `/jobs` y `/pubsub`, más dashboard y navegación.
- Tests backend contra **Postgres y Redis reales** (Testcontainers): **39 verdes**.

## Stack

- **Backend**: Java 21 + Spring Boot 4.x
- **Frontend**: Next.js (App Router, TypeScript, Tailwind)
- **Datos**: PostgreSQL + Redis (Docker Compose)

## Estructura

```
apps/
  backend/    Spring Boot — un solo proyecto con módulos por concepto
  web/        Next.js — una página por módulo
compose.yaml  Postgres + Redis
```

> `docs/PLAN.md` es una nota de trabajo local (no versionada).

## Dev

Requisitos: JDK 21, Node.js 24+, Docker.

```bash
# Infraestructura (Postgres + Redis)
docker compose up -d

# API (desde apps/backend) → http://localhost:8000
./mvnw spring-boot:run

# Web (desde apps/web) → http://localhost:3000
npm install
npm run dev
```

Comprobación: `curl http://localhost:8000/healthz`
