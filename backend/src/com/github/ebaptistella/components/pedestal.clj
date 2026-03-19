(ns com.github.ebaptistella.components.pedestal
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.config.reader :as config.reader]
            [com.github.ebaptistella.interceptors.logging :as interceptors.logging]
            [com.github.ebaptistella.interceptors.validation :as interceptors.validation]
            [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [schema.core :as s]))

(defrecord PedestalComponent [server-config config logger server jetty-server system]
  component/Lifecycle
  (start [this]
    (if server
      this
      (let [base-config  (or server-config {})
            base-port    (::http/port base-config)
            final-config (if config
                           (let [port (config.reader/http->port config)
                                 host (config.reader/http->host config)
                                 cfg  (if (and port (not= 0 base-port))
                                        (assoc base-config ::http/port port)
                                        base-config)]
                             (if host (assoc cfg ::http/host host) cfg))
                           base-config)
            full-system  {:logger logger :config config :pedestal this}
            ctx-interceptor (let [captured full-system]
                              (interceptor/interceptor
                               {:name ::inject-context
                                :enter (fn [ctx]
                                         (let [sys (or (get-in ctx [:request ::http/context :system]) captured)]
                                           (-> ctx
                                               (assoc-in [:request ::http/context] {:system sys})
                                               (assoc-in [:request :components]
                                                         {:logger  (:logger sys)
                                                          :config  (:config sys)
                                                          :pedestal (:pedestal sys)}))))}))
            srv-cfg (-> (assoc final-config ::http/context {:system full-system})
                        http/default-interceptors
                        http/dev-interceptors
                        (update ::http/interceptors
                                #(concat % [ctx-interceptor
                                            interceptors.logging/logging-interceptor
                                            interceptors.validation/json-body
                                            interceptors.validation/json-response
                                            interceptors.validation/error-handler-interceptor])))
            started (http/start (http/create-server srv-cfg))
            log     (logger/bound logger)]
        (logger/log-call log :info "[Pedestal] Server started on %s:%d"
                         (::http/host final-config) (::http/port final-config))
        (assoc this :server started :jetty-server (::http/server started) :system full-system))))

  (stop [this]
    (when server
      (http/stop server)
      (logger/log-call (logger/bound logger) :info "[Pedestal] Server stopped"))
    (dissoc this :server :jetty-server :system)))

(s/defn new-pedestal [server-config]
  (map->PedestalComponent {:server-config server-config}))
