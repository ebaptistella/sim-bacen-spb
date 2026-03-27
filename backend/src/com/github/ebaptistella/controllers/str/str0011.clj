(ns com.github.ebaptistella.controllers.str.str0011
  "STR0011: cancelamento manual de lançamento pendente. R1=CANCELADO, E=rejeitado."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.controllers.str.str :refer [available-responses process! respond!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0011 :as logic.str0011]
            [schema.core :as s])
  (:import [java.time Instant]))

(def ^:private accepted-response-types #{:STR0011R1 :STR0011E})

(defmethod available-responses :STR0011 [_msg] [:STR0011R1 :STR0011E])

(defmethod process! :STR0011
  [msg {:keys [store logger]}]
  ;; Logic Sandwich: query first → effect
  (logger/log-call logger :info "[STR] STR0011 received | id=%s num-ctrl-str-or=%s"
                   (:message-id msg) (:num-ctrl-str-or msg))
  (let [original (store.messages/find-by-num-ctrl-if store (:num-ctrl-str-or msg))
        entry    (assoc msg :original-msg-id (:id original))]
    (store.messages/save! store entry)))

(defmethod respond! :STR0011
  [msg {:keys [store mq-cfg]} {:keys [response-type params]}]
  (cond
    (= :responded (:status msg))
    {:error :already-responded}

    (not (accepted-response-types response-type))
    {:error :invalid-response-type}

    :else
    (let [fields (case response-type
                   :STR0011R1 (logic.str0011/r1-response msg params)
                   :STR0011E  (logic.str0011/rejection-response msg params))]
      (if (= :missing-motivo (:error fields))
        fields
        (let [xml        (logic.str0011/response->xml response-type fields)
              queue-name (parser/r1-outbound-queue (:queue-name msg))]
          (mq.producer/send-message! mq-cfg queue-name xml)
          (store.messages/update-message! store (:id msg)
                                          #(-> %
                                               (assoc :status :responded)
                                               (update :responses (fnil conj [])
                                                       {:type    response-type
                                                        :body    xml
                                                        :sent-at (str (Instant/now))})))
          {:ok true})))))
