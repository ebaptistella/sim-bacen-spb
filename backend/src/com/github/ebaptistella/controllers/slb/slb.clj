(ns com.github.ebaptistella.controllers.slb.slb
  "SLB message processing dispatch: ingest and respond."
  (:require [schema.core :as s]))

(defmulti process!
  "Process inbound SLB message (save to store)."
  (fn [msg _components] (keyword (:type msg))))

(defmulti send-message!
  "Inject SLB message via HTTP: validate, build XML, publish to MQ."
  (fn [msg-type _data _components] (keyword msg-type)))

(s/defn get-msg-type :- s/Keyword
  "Safely get message type as keyword."
  [msg-type-str :- s/Str]
  (keyword msg-type-str))
