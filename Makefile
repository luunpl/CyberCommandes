.DEFAULT_GOAL := help
# Prefer Docker Compose V2 (`docker compose`); fall back to the legacy
# standalone V1 binary (`docker-compose`) if the V2 plugin is not installed.
COMPOSE := $(shell if docker compose version >/dev/null 2>&1; then echo "docker compose"; else echo "docker-compose"; fi)

.PHONY: help
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

## ---- Local development ----
.PHONY: db
db: ## Start only the local PostgreSQL database
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
up: ## Build and start the whole stack (db + backend + frontend)
	$(COMPOSE) up --build -d

.PHONY: down
down: ## Stop the stack
	$(COMPOSE) down

.PHONY: logs
logs: ## Tail stack logs
	$(COMPOSE) logs -f

.PHONY: seed
seed: ## Load demo domain data into the running DB (run after `make up`)
	$(COMPOSE) exec -T postgres psql -U $${POSTGRES_USER:-postgres} -d $${POSTGRES_DB:-local} < docker/seed-data.sql

.PHONY: monitoring
monitoring: ## Start the stack with Prometheus + Grafana
	$(COMPOSE) --profile monitoring up --build -d

## ---- Kubernetes ----
.PHONY: k8s-deploy
k8s-deploy: ## Deploy to the current kube-context
	kubectl apply -k k8s/

.PHONY: k8s-delete
k8s-delete: ## Remove the k8s deployment
	kubectl delete -k k8s/
