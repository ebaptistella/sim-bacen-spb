(ns com.github.ebaptistella.system
  (:require [com.github.ebaptistella.components.configuration :as components.configuration]
            [com.github.ebaptistella.components.logger :as components.logger]
            [com.github.ebaptistella.components.mq-worker :as components.mq-worker]
            [com.github.ebaptistella.components.pedestal :as components.pedestal]
            [com.github.ebaptistella.config.reader :as config.reader]
            [com.github.ebaptistella.handlers.http-server :as handlers.http-server]
            [com.stuartsierra.component :as component]
            [schema.core :as s]))

(s/defn new-system
  ([]
   (new-system {}))
  ([{:keys [server-config logger-name]
     :or {server-config handlers.http-server/server-config
          logger-name config.reader/default-application-name}}]
   (component/system-map
    :logger (components.logger/new-logger logger-name)
    :config (component/using
             (components.configuration/new-config config.reader/default-config-file)
             [:logger])
    :mq-worker (component/using
                (components.mq-worker/new-mq-worker)
                [:config :logger])
    :pedestal (component/using
               (components.pedestal/new-pedestal server-config)
               [:config :logger]))))

(s/defn new-dev-system
  []
  (new-system {:server-config handlers.http-server/server-config}))
