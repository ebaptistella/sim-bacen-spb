(ns com.github.ebaptistella.integration.aux.init
  "Initializes the system for integration tests."
  (:require [com.github.ebaptistella.handlers.http-server :as http-server]
            [com.github.ebaptistella.system :as system]
            [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]))

(defonce test-system (atom nil))

(defn start-test-system! []
  (when (nil? @test-system)
    (reset! test-system
            (component/start-system
             (system/new-system
              {:server-config (assoc http-server/server-config ::http/port 0)}))))
  @test-system)

(defn stop-test-system! []
  (when @test-system
    (component/stop-system @test-system)
    (reset! test-system nil)))
