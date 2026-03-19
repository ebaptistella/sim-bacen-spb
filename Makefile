SHELL := /bin/bash

ROOT_DIR := $(abspath $(dir $(lastword $(MAKEFILE_LIST))))

.PHONY: help up down build logs \
        backend-logs frontend-logs ibmmq-logs \
        ibmmq-status ibmmq-gen-queues

COMPOSE := docker compose -f infra/docker-compose.yml --env-file .env

help:
	@echo "Available targets:"
	@echo "  up                 - Generate MQSC file and start full stack"
	@echo "  down               - Stop full stack"
	@echo "  build              - Build backend and frontend images"
	@echo "  logs               - Tail all service logs"
	@echo "  backend-logs       - Tail backend logs"
	@echo "  frontend-logs      - Tail frontend logs"
	@echo "  ibmmq-logs         - Tail IBM MQ logs"
	@echo "  ibmmq-status       - Show all container status"
	@echo "  ibmmq-gen-queues   - Generate infra/ibmmq/20-spb-queues.mqsc from .env"

# ---------------------------------------------------------------------------
# Build
# ---------------------------------------------------------------------------
build:
	$(COMPOSE) build backend frontend

# ---------------------------------------------------------------------------
# Full stack
# ---------------------------------------------------------------------------
up: ibmmq-gen-queues
	$(COMPOSE) up -d
	@echo "Stack up: ibmmq, backend (:3000), frontend (:8080)"

down:
	$(COMPOSE) down
	@echo "Stack stopped."

# ---------------------------------------------------------------------------
# Logs
# ---------------------------------------------------------------------------
logs:
	$(COMPOSE) logs -f

backend-logs:
	$(COMPOSE) logs -f backend

frontend-logs:
	$(COMPOSE) logs -f frontend

ibmmq-logs:
	$(COMPOSE) logs -f ibmmq

# ---------------------------------------------------------------------------
# IBM MQ
# ---------------------------------------------------------------------------
ibmmq-gen-queues:
	cd infra/ibmmq && bash gen-setup-spb-queues.sh

ibmmq-status:
	$(COMPOSE) ps
