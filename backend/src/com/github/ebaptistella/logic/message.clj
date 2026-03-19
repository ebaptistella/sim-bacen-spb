(ns com.github.ebaptistella.logic.message
  "Pure business logic for SPB message processing.
   No I/O, no side effects - 100% testable."
  (:require [schema.core :as s]))

(s/defn process
  "Processes a raw SPB message and returns the result.
   msg: map with :queue-name, :message-id, :body"
  [msg]
  {:message-id (:message-id msg)
   :queue-name (:queue-name msg)
   :status     :processed})
