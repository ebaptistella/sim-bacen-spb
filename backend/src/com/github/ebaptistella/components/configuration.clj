(ns com.github.ebaptistella.components.configuration
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.github.ebaptistella.components.logger :as logger]
            [com.stuartsierra.component :as component]
            [schema.core :as s]))

(s/defn ^:private load-config-file
  [config-file logger]
  (try
    (let [resource (io/resource config-file)]
      (if resource
        (let [config (edn/read-string (slurp resource))
              log (logger/bound logger)]
          (logger/log-call log :info "[Config] Configuration file loaded: %s" config-file)
          config)
        (throw (ex-info (format "Configuration file not found: %s" config-file)
                        {:config-file config-file}))))
    (catch Exception e
      (when logger
        (logger/log-call (logger/bound logger) :error
                         "[Config] Error loading configuration file: %s" config-file e))
      (throw e))))

(defrecord ConfigComponent [config-file logger config]
  component/Lifecycle
  (start [this]
    (if config
      this
      (assoc this :config (load-config-file config-file logger))))
  (stop [this]
    (dissoc this :config)))

(s/defn new-config [config-file]
  (map->ConfigComponent {:config-file config-file}))

(s/defn get-config [config-component]
  (:config config-component))
