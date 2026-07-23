# syntax=docker/dockerfile:1

# ---- Stage 1: build the Angular app ----
FROM node:26-alpine AS build
WORKDIR /app

# Install dependencies from the lockfile first for better layer caching.
COPY package.json package-lock.json ./
RUN npm ci

# Build the production (static) browser bundle.
COPY . .
RUN npm run build

# ---- Stage 2: serve with nginx ----
FROM nginx:1.27-alpine AS runtime

# SPA + reverse-proxy config.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# angular.json has ssr=false, so the build emits a static bundle under
# dist/projet_int/browser.
COPY --from=build /app/dist/projet_int/browser /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD ["sh", "-c", "wget -q --spider http://localhost:80/ || exit 1"]
