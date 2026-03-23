(ns com.github.ebaptistella.controllers.message
  "Orchestrates the processing of SPB messages received from IBM MQ.
   Follows the Logic Sandwich pattern: wire-in → logic → wire-out."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.logic.message :as logic.message]
            [schema.core :as s]))

(s/defn process!
  "Processes a single SPB message.
   msg: map with :queue-name, :message-id, :body"
  [msg _mq-cfg log]
  (logger/log-call log :info "[MessageController] Processing message | queue: %s | id: %s"
                   (:queue-name msg) (:message-id msg))
  (try
    (let [result (logic.message/process msg)]
      (logger/log-call log :info "[MessageController] Message processed | id: %s | result: %s"
                       (:message-id msg) result)
      result)
    (catch Exception e
      (logger/log-call log :error "[MessageController] Failed | id: %s | error: %s"
                       (:message-id msg) (.getMessage e))
      (throw e))))
