(ns com.github.ebaptistella.controllers.str.str
  "STR inbound message dispatcher.
   Uses defmulti dispatching on :type so each STR message type
   registers its own defmethod in a specialized controller namespace."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]))

(defmulti process!
  "Processes a parsed STR inbound domain model.
   Input:      domain map from wire/in/str/str
   components: {:store :logger :mq-cfg}
   Dispatch:   (:type msg)"
  (fn [msg _components]
    (:type msg)))

(defmethod process! :default
  [msg {:keys [store logger]}]
  (let [log (logger/bound logger)]
    (logger/log-call log :warn
                     "[STR] No handler for type=%s id=%s — storing as :pending"
                     (str (:type msg)) (:id msg))
    (store.messages/save! store msg)))
