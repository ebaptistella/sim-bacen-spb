(ns com.github.ebaptistella.components.logger
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s])
  (:import (org.slf4j LoggerFactory)))

(def default-logger-name
  "Default logger name used when no logger-name is provided."
  "app")

(defprotocol ILogger
  (info [this message args])
  (debug [this message args])
  (warn [this message args])
  (error [this message args throwable]))

(defrecord LoggerComponent [logger-name logger]
  component/Lifecycle
  (start [this]
    (if logger
      this
      (assoc this :logger (LoggerFactory/getLogger (or logger-name default-logger-name)))))
  (stop [this]
    (dissoc this :logger))

  ILogger
  (info [_this message args]
    (when logger
      (if (seq args) (.info logger (apply format message args)) (.info logger message))))
  (debug [_this message args]
    (when logger
      (if (seq args) (.debug logger (apply format message args)) (.debug logger message))))
  (warn [_this message args]
    (when logger
      (if (seq args) (.warn logger (apply format message args)) (.warn logger message))))
  (error [_this message args throwable]
    (when logger
      (let [msg (if (seq args) (apply format message args) message)]
        (if throwable (.error logger msg throwable) (.error logger msg))))))

(s/defn bound
  "Returns a map of logging functions with the logger-component already bound."
  [logger-component]
  {:info  (fn [message & args] (when logger-component (info logger-component message args)))
   :debug (fn [message & args] (when logger-component (debug logger-component message args)))
   :warn  (fn [message & args] (when logger-component (warn logger-component message args)))
   :error (fn [message & args]
            (when logger-component
              (let [has-throwable? (and (seq args) (instance? Throwable (last args)))
                    [fmt-args t] (if has-throwable? [(butlast args) (last args)] [args nil])]
                (error logger-component message fmt-args t))))})

(s/defn log-call
  "Calls a logging function from a bound logger. Levels: :info :debug :warn :error"
  [bound-logger log-level & args]
  (when-let [log-fn (get bound-logger log-level)]
    (apply log-fn args)))

(s/defn new-logger
  ([] (new-logger default-logger-name))
  ([logger-name] (map->LoggerComponent {:logger-name logger-name})))
