(ns com.github.ebaptistella.handlers.routes.health
  (:require [com.github.ebaptistella.infrastructure.http-server.health :as http-server.health]))

(def routes
  #{["/api/health"
     :get
     http-server.health/health-check
     :route-name :health-check]})
