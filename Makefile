SHELL := /bin/bash

ROOT_DIR := $(abspath $(dir $(lastword $(MAKEFILE_LIST))))

.PHONY: help up down ibmmq-up ibmmq-down ibmmq-logs ibmmq-status ibmmq-gen-queues

COMPOSE_ENV := --env-file ../.env

help:
	@echo "Available targets:"
	@echo "  up                 - Generate MQSC file and start IBM MQ"
	@echo "  down               - Stop IBM MQ docker-compose stack"
	@echo "  ibmmq-up           - Start IBM MQ docker-compose stack"
	@echo "  ibmmq-down         - Stop IBM MQ docker-compose stack"
	@echo "  ibmmq-logs         - Tail IBM MQ container logs"
	@echo "  ibmmq-status       - Show IBM MQ container status"
	@echo "  ibmmq-gen-queues   - Generate dev/ibmmq/20-spb-queues.mqsc from .env"

ibmmq-up:
	cd dev && docker compose $(COMPOSE_ENV) up -d

ibmmq-down:
	cd dev && docker compose $(COMPOSE_ENV) down

ibmmq-logs:
	cd dev && docker compose $(COMPOSE_ENV) logs -f ibmmq

ibmmq-status:
	cd dev && docker compose $(COMPOSE_ENV) ps

ibmmq-gen-queues:
	cd dev/ibmmq && bash gen-setup-spb-queues.sh

# Full flow: generate MQSC (20-spb-queues.mqsc) -> start MQ (it applies MQSC on startup)
up: ibmmq-gen-queues ibmmq-up
	@echo "Environment is up with IBM MQ and MQSC (20-spb-queues.mqsc) applied on startup."

down: ibmmq-down
	@echo "IBM MQ docker-compose stack stopped."

