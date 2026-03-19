(ns com.github.ebaptistella.handlers.routes.health
  (:require [com.github.ebaptistella.infrastructure.http-server.health :as http-server.health]
            [com.github.ebaptistella.wire.out.health :as wire.out.health]))

(def routes
  #{["/api/health"
     :get
     http-server.health/health-check
     :route-name :health-check]})
