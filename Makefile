.DEFAULT_GOAL := help
# This stack uses the modern Compose Spec, which requires Docker Compose V2.
# The legacy V1 `docker-compose` binary cannot parse it.
COMPOSE := docker compose

.PHONY: _require-compose
_require-compose:
	@docker compose version >/dev/null 2>&1 || { \
	  echo "==> Docker Compose V2 is required (this project uses the modern Compose Spec)."; \
	  echo "    The legacy 'docker-compose' V1 cannot run it. Install V2:"; \
	  echo "    https://docs.docker.com/compose/install/linux/"; \
	  exit 1; }

.PHONY: help
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

## ---- Local development ----
.PHONY: db
db: _require-compose ## Start only the local PostgreSQL database
	$(COMPOSE) -f docker/docker-compose-local.yml up -d

.PHONY: backend
backend: ## Run the Spring Boot backend locally
	mvn -f pom.xml -pl :server -am spring-boot:run

.PHONY: frontend
frontend: ## Run the Angular dev server
	cd client && npm start

## ---- Build & test ----
.PHONY: test
test: test-backend test-frontend ## Run all tests

.PHONY: test-backend
test-backend: ## Run backend tests
	mvn -B -f pom.xml -pl :server -am verify

.PHONY: test-frontend
test-frontend: ## Run frontend tests
	cd client && npm ci && npm test -- --watch=false

## ---- Docker (full stack) ----
.PHONY: up
up: _require-compose ## Build and start the whole stack (db + backend + frontend)
	$(COMPOSE) up --build -d

.PHONY: down
down: _require-compose ## Stop the stack
	$(COMPOSE) down

.PHONY: logs
logs: _require-compose ## Tail stack logs
	$(COMPOSE) logs -f

.PHONY: seed
seed: _require-compose ## Load demo domain data into the running DB (run after `make up`)
	$(COMPOSE) exec -T postgres psql -U $${POSTGRES_USER:-postgres} -d $${POSTGRES_DB:-local} < docker/seed-data.sql

.PHONY: monitoring
monitoring: _require-compose ## Start the stack with Prometheus + Grafana
	$(COMPOSE) --profile monitoring up --build -d

## ---- Kubernetes ----
.PHONY: k8s-deploy
k8s-deploy: ## Deploy to the current kube-context
	kubectl apply -k k8s/

.PHONY: k8s-delete
k8s-delete: ## Remove the k8s deployment
	kubectl delete -k k8s/
