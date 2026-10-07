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

Proyecto **en construcción**, full-stack por módulo. **Módulo 0 (fundación)** y **Módulo 1 (caché)** cerrados:

- Backend Spring Boot con `GET /healthz`, **Postgres** (JPA + Flyway) y **Redis** (Spring Cache) cableados.
- Endpoints de caché: `GET /cache/products/{id}` (lectura con **caché *cache-aside***, TTL 60 s; `?cached=false` para leer **sin** caché), `DELETE /cache/products/{id}` (invalidar una entrada) y `GET /cache/products/bench` (benchmark de concurrencia).
- Métricas: `/actuator/metrics/cache.gets` (hits/misses → ***hit ratio***) y `/actuator/health`.
- Frontend (**Next.js**): página `/cache` con un **benchmark** (N peticiones, C concurrentes) que compara *sin caché* vs *con caché*, más un dashboard y navegación.
- Tests backend contra **Postgres y Redis reales** (Testcontainers): **16 verdes**.

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
