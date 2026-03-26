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
  (logger/log-call logger :warn
                   "[STR] No handler for type=%s id=%s — storing as :pending"
                   (str (:type msg)) (:id msg))
  (store.messages/save! store msg))

(defmulti respond!
  "Responds to a pending inbound STR message.
   msg:        domain map from store (already fetched)
   components: {:store :mq-cfg}
   req:        {:response-type s/Str :params {}}
   Dispatch:   (:type msg)"
  (fn [msg _components _req]
    (:type msg)))

(defmethod respond! :default
  [msg {:keys [logger]} _req]
  (when logger
    (logger/log-call logger :warn
                     "[STR] No respond! handler for type=%s"
                     (str (:type msg))))
  {:error :unsupported-type})
