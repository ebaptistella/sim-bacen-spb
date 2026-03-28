(ns com.github.ebaptistella.controllers.str.str0043
  "STR0043: Agendamento de teste de contingência Internet. Fluxo1: R1 apenas."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.controllers.str.str :refer [available-responses process! respond!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0043 :as logic.str0043])
  (:import [java.time Instant]))

(def ^:private accepted-response-types #{:STR0043R1})

(defmethod available-responses :STR0043 [_msg] [:STR0043R1])

(defmethod process! :STR0043
  [msg {:keys [store logger]}]
  (logger/log-call logger :info "[STR] STR0043 received | id=%s type=%s"
                   (:message-id msg) (:type msg))
  (store.messages/save! store msg))

(defmethod respond! :STR0043
  [msg {:keys [store mq-cfg]} {:keys [response-type params]}]
  (cond
    (= :responded (:status msg))
    {:error :already-responded}

    (not (accepted-response-types response-type))
    {:error :invalid-response-type}

    :else
    (let [fields     (logic.str0043/r1-response msg (or params {}))
          xml        (logic.str0043/response->xml response-type fields)
          queue-name (parser/r1-outbound-queue (:queue-name msg))]
      (mq.producer/send-message! mq-cfg queue-name xml)
      (store.messages/update-message! store (:id msg)
                                      #(-> %
                                           (assoc :status :responded)
                                           (update :responses (fnil conj [])
                                                   {:type    response-type
                                                    :body    xml
                                                    :sent-at (str (Instant/now))})))
      {:ok true})))
