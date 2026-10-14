# springboot-redis-workshop

🌐 [English](README.md) · **Español**

<p align="center">
  <img src="docs/images/00_Hero.jpg" width="520" alt="Spring Boot / Redis Workshop">
</p>

**Un taller práctico para aprender Spring Boot + Redis.** Una colección de mini-módulos
independientes (caché, rate limiting, leaderboard, cola de trabajos, pub/sub, lock distribuido...)
para aprender ambas tecnologías construyendo, probando y trasteando desde un frontend.

> **Objetivo:** aprender Spring Boot y Redis. El proyecto es la excusa; entender cada línea es el
> resultado.

## Módulos

Cada módulo demuestra un patrón de Redis y trae su propia API, sus tests y su página en el frontend.

### 1 — Caché

<p align="center"><img src="docs/images/01_Cache.jpg" width="420" alt="Caché"></p>

Demostramos la diferencia de velocidad al acceder a productos aleatorios de una lista, **con y sin
caché**. Lanzamos un número de peticiones con distinta concurrencia, planteamos varios escenarios y
comparamos los resultados.

### 2 — Rate limiting

<p align="center"><img src="docs/images/02_Rate_Limiting.jpg" width="420" alt="Rate limiting"></p>

Un **token bucket** limita cuántas peticiones acepta un endpoint por IP. Vemos cómo el cubo se
rellena con el tiempo, cuándo devuelve `429` y cómo reparte la carga en vez de dejarla pasar toda de
golpe.

### 3 — Leaderboard

<p align="center"><img src="docs/images/03_Leaderboard.jpg" width="420" alt="Leaderboard"></p>

Comparamos cómo de rápido se actualiza un ranking con un **sorted set** de Redis frente a hacerlo
con `UPDATE`s en Postgres, bajo mucha concurrencia. Medimos operaciones por segundo.

### 4 — Sesiones

<p align="center"><img src="docs/images/04_Sessions.jpg" width="420" alt="Sesiones"></p>

Login con la sesión guardada en **Redis** (hash + TTL) y el token en una cookie `HttpOnly`. Vemos
cómo caduca sola, el control de acceso por rol y el contador del TTL en vivo.

### 5 — Cola de trabajos

<p align="center"><img src="docs/images/05_Work_Queue.jpg" width="420" alt="Cola de trabajos"></p>

Encolamos tareas y las procesamos **en background** con varios workers, sin bloquear la petición.
Vemos el reparto, el `ack`, los reintentos y el rescate de las tareas que se quedan colgadas.

### 6 — Pub/Sub

<p align="center"><img src="docs/images/06_Pub_Sub.jpg" width="420" alt="Pub/Sub"></p>

Un **reloj en vivo**: el servidor publica eventos en un canal de Redis y los empuja al navegador con
**SSE**. Comparamos preguntar (polling) frente a que el servidor empuje, y lo usamos para ver el rate
limiting y el TTL de sesiones en tiempo real.

### 7 — Lock distribuido

<p align="center"><img src="docs/images/07_Shared_Lock.jpg" width="420" alt="Lock distribuido"></p>

Un recurso al que solo puede acceder uno a la vez. Con un lock en Redis (`SET NX PX`) gestionamos
quién tiene "la llave", cómo la libera (solo el dueño) y cómo se libera sola si el dueño se cae.

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
