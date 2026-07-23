# CyberCommandes

> Delivery / tour-planning management application — Projet Intégrateur 2025-2026 (Université Grenoble Alpes).

[![CI](https://github.com/luunpl/cybercommandes/actions/workflows/ci.yml/badge.svg)](https://github.com/luunpl/cybercommandes/actions/workflows/ci.yml)
[![CD](https://github.com/luunpl/cybercommandes/actions/workflows/cd.yml/badge.svg)](https://github.com/luunpl/cybercommandes/actions/workflows/cd.yml)

A full-stack application built with an **Angular 21** frontend and a
**Spring Boot 4 / Java 21** multi-module backend, backed by **PostgreSQL**.
The repository is fully containerized and ships with CI/CD, image scanning,
Kubernetes manifests and a Prometheus/Grafana monitoring stack.

## Architecture

```
        ┌────────────┐     /api      ┌────────────┐      JDBC      ┌────────────┐
Browser │  frontend  │ ────────────▶ │  backend   │ ─────────────▶ │ PostgreSQL │
────────▶  (nginx)   │               │(Spring Boot)│               │            │
        └────────────┘               └─────┬──────┘                └────────────┘
                                           │ /actuator/prometheus
                                           ▼
                                  ┌──────────────────┐
                                  │ Prometheus + Grafana │
                                  └──────────────────┘
```

| Layer      | Tech                                             |
|------------|--------------------------------------------------|
| Frontend   | Angular 21, Leaflet, served by nginx             |
| Backend    | Spring Boot 4, Java 21, JPA, MapStruct, Actuator |
| Database   | PostgreSQL 16                                     |
| CI/CD      | GitHub Actions, GHCR, Trivy, Dependabot          |
| Orchestr.  | Docker Compose, Kubernetes (kustomize)           |
| Monitoring | Prometheus, Grafana                              |

## Quick start (Docker)

The whole stack runs with a single command:

```shell
cp .env.example .env        # adjust values if needed
make up                     # or: docker compose up --build -d
```

| Service    | URL                                    |
|------------|----------------------------------------|
| Frontend   | http://localhost:4200                  |
| Backend    | http://localhost:8080                  |
| Swagger UI | http://localhost:8080/swagger-ui.html  |
| Health     | http://localhost:8080/actuator/health  |

With monitoring (`make monitoring`): Grafana on http://localhost:3000
(admin / `GRAFANA_ADMIN_PASSWORD`), Prometheus on http://localhost:9090.

Run `make help` to list every available command.

## Kubernetes

Manifests live in [`k8s/`](k8s/) and are assembled with kustomize:

```shell
kubectl apply -k k8s/       # or: make k8s-deploy
```

Set the image tags produced by the CD pipeline before deploying:

```shell
cd k8s
kustomize edit set image \
  ghcr.io/luunpl/cybercommandes-backend=ghcr.io/luunpl/cybercommandes-backend:<tag>
```

## CI/CD

- **CI** (`.github/workflows/ci.yml`) — builds and tests the backend
  (`mvn verify`, H2) and the frontend (`npm ci`, unit tests, production build)
  on every push and pull request.
- **CD** (`.github/workflows/cd.yml`) — builds the backend and frontend Docker
  images, scans them with **Trivy** (results uploaded to the GitHub Security
  tab), and pushes them to **GHCR** on `main` and version tags.
- **Dependabot** keeps Maven, npm, GitHub Actions and Docker base images up to
  date.

---

## Développement (FR)

### Base de données locale

Vous pouvez créer une base de données locale via
[`docker/docker-compose-local.yml`](./docker/docker-compose-local.yml).

#### Prérequis

1. [Docker](https://docs.docker.com/engine/install/) et
   [docker-compose](https://docs.docker.com/compose/install/) installés.

#### Démarrage / arrêt

```shell
docker compose -f docker/docker-compose-local.yml up --detach
docker compose -f docker/docker-compose-local.yml down
```

### Variables d'environnement

Copiez `.env.example` vers `.env`. Les variables consommées par le backend :

```dotenv
DB_USERNAME=<username>
DB_PASSWORD=<password>
DB_URL=jdbc:postgresql://<server>:<port>/<database>
DB_JPA_DDL_AUTO=<create-drop | create | update | validate | none>
```

### Compiler et démarrer le serveur

```shell
mvn -f pom.xml -pl :server -am clean install   # compiler
make backend                                   # démarrer Spring Boot
```

### Frontend

La doc d'installation du frontend est dans
[`client/README-INSTALLATION.md`](client/README-INSTALLATION.md).

```shell
cd client && npm install && npm start
```
