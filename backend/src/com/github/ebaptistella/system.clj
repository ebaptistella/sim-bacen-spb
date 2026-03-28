(ns com.github.ebaptistella.system
  (:require [com.github.ebaptistella.components.configuration :as components.configuration]
            [com.github.ebaptistella.components.logger :as components.logger]
            [com.github.ebaptistella.components.mq-worker :as components.mq-worker]
            [com.github.ebaptistella.components.pedestal :as components.pedestal]
            [com.github.ebaptistella.components.store :as components.store]
            [com.github.ebaptistella.config.reader :as config.reader]
            [com.github.ebaptistella.controllers.str.str0008]
            [com.github.ebaptistella.controllers.str.str0010]
            [com.github.ebaptistella.controllers.str.str0025]
            [com.github.ebaptistella.controllers.str.str0034]
            [com.github.ebaptistella.controllers.str.str0037]
            [com.github.ebaptistella.controllers.str.str0039]
            [com.github.ebaptistella.controllers.str.str0041]
            [com.github.ebaptistella.controllers.str.str0047]
            [com.github.ebaptistella.controllers.str.str0048]
            [com.github.ebaptistella.controllers.str.str0051]
            [com.github.ebaptistella.controllers.str.str0052]
            [com.github.ebaptistella.handlers.http-server :as handlers.http-server]
            [com.github.ebaptistella.wire.in.str.str0008]
            [com.github.ebaptistella.wire.in.str.str0010]
            [com.github.ebaptistella.wire.in.str.str0025]
            [com.github.ebaptistella.wire.in.str.str0034]
            [com.github.ebaptistella.wire.in.str.str0037]
            [com.github.ebaptistella.wire.in.str.str0039]
            [com.github.ebaptistella.wire.in.str.str0041]
            [com.github.ebaptistella.wire.in.str.str0047]
            [com.github.ebaptistella.wire.in.str.str0048]
            [com.github.ebaptistella.wire.in.str.str0051]
            [com.github.ebaptistella.wire.in.str.str0052]
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
    :store  (components.store/new-store)
    :mq-worker (component/using
                (components.mq-worker/new-mq-worker)
                [:config :logger :store])
    :pedestal (component/using
               (components.pedestal/new-pedestal server-config)
               [:config :logger :store]))))

(s/defn new-dev-system
  []
  (new-system {:server-config handlers.http-server/server-config}))
