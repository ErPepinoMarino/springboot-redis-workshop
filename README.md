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
| **Cola de trabajos** | listas / Streams | trabajo en background |
| **Pub/Sub** | canales | notificaciones en vivo (SSE) |
| **Lock distribuido** | `SET NX PX` | exclusión mutua entre procesos |

## Estado actual

Proyecto **en construcción**, full-stack por módulo. Cerrados: **Módulo 0 (fundación)**, **Módulo 1 (caché)**, **Módulo 2 (rate limiting)** y **Módulo 3 (leaderboard)**.

- Backend Spring Boot con `GET /healthz`, **Postgres** (JPA + Flyway) y **Redis** cableados; Actuator (`/actuator/health`, `/actuator/metrics`).
- **Módulo 1 — Caché**: `GET`/`DELETE /cache/products/{id}`, `?cached=false`, `GET /cache/products/bench` (benchmark de concurrencia), *cache-aside* con Redis y TTL 60 s.
- **Módulo 2 — Rate limiting**: **token bucket** atómico en **Redis + Lua**; `GET /ratelimit/ping` limitado por IP (`429` + `Retry-After` + `X-RateLimit-*`) y `GET /ratelimit/simulate` (modelo de simulación: pasa/cola/descartadas + ritmo de servicio).
- **Módulo 3 — Leaderboard**: **test de carga** con *sorted sets* de Redis (`ZINCRBY`) vs Postgres (`UPDATE` en transacción): `POST /leaderboard/benchmark` mide las **operaciones completadas por segundo** bajo concurrencia (Redis escala; Postgres se techa en su pool).
- Frontend (**Next.js**): páginas `/cache`, `/ratelimit` y `/leaderboard`, más dashboard y navegación.
- Tests backend contra **Postgres y Redis reales** (Testcontainers): **25 verdes**.

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
