# ---------- Etapa 1: BUILD ----------
FROM node:24-alpine AS build
WORKDIR /app

# BACKEND_URL del compose para que machaque al localhost:8000
ARG BACKEND_URL=http://localhost:8000
ENV BACKEND_URL=$BACKEND_URL

# Cacheamos dependencias
COPY package.json package-lock.json ./
RUN npm ci

# Ahora el código y el build
COPY . .
RUN npm run build

# ---------- Etapa 2: RUNTIME ----------
FROM node:24-alpine
WORKDIR /app
ENV NODE_ENV=production

# El servidor "standalone" + estáticos + públicos
COPY --from=build /app/public ./public
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static

EXPOSE 3000
CMD ["node", "server.js"]