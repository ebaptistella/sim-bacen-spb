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

(defn test-base-url
  "URL base HTTP do Pedestal (porta efetiva quando ::http/port é 0)."
  []
  (when-let [sys @test-system]
    (when-let [jetty (:jetty-server (:pedestal sys))]
      (when-let [port (try (-> jetty .getConnectors first .getLocalPort)
                            (catch Exception _ nil))]
        (str "http://127.0.0.1:" port)))))

(defn test-store
  "Store component do sistema de integração."
  []
  (:store @test-system))
