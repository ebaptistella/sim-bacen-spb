(ns com.github.ebaptistella.controllers.str.str0046
  "STR0046: Devolução de repasse de tributos. Fluxo2: R1 + R2 + E."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.controllers.str.str :refer [available-responses process! respond!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0046 :as logic.str0046])
  (:import [java.time Instant]))

(def ^:private accepted-response-types #{:STR0046R1 :STR0046R2 :STR0046E})

(defmethod available-responses :STR0046 [_msg] [:STR0046R1 :STR0046R2 :STR0046E])

(defmethod process! :STR0046
  [msg {:keys [store logger]}]
  (logger/log-call logger :info "[STR] STR0046 received | id=%s type=%s"
                   (:message-id msg) (:type msg))
  (store.messages/save! store msg))

(defmethod respond! :STR0046
  [msg {:keys [store mq-cfg]} {:keys [response-type params]}]
  (cond
    (and (= :responded (:status msg)) (not= :STR0046R2 response-type))
    {:error :already-responded}

    (not (accepted-response-types response-type))
    {:error :invalid-response-type}

    (and (= :responded (:status msg)) (= :STR0046R2 response-type) (store.messages/response-sent? msg :STR0046R2))
    {:error :r2-already-sent}

    (and (= :pending (:status msg)) (= :STR0046R2 response-type))
    {:error :r2-requires-r1}

    :else
    (let [p      (cond-> (or params {})
                   (= :STR0046R2 response-type)
                   (assoc :NumCtrlSTR (-> msg :responses first :num-ctrl-str)))
          fields (case response-type
                   :STR0046R1 (logic.str0046/r1-response msg p)
                   :STR0046R2 (logic.str0046/r2-response msg p)
                   :STR0046E  (logic.str0046/rejection-response msg p))]
      (if (= :missing-motivo (:error fields))
        fields
        (let [xml        (logic.str0046/response->xml response-type fields)
              queue-name (cond
                           (#{:STR0046R1 :STR0046E} response-type)
                           (parser/r1-outbound-queue (:queue-name msg))

                           (= :STR0046R2 response-type)
                           (parser/r2-outbound-queue (:queue-name msg) (:ispb-if-credtd msg)))]
          (when (nil? queue-name)
            (throw (ex-info "Cannot derive outbound queue for STR0046R2 (missing ISPBIFCredtd?)" {})))
          (mq.producer/send-message! mq-cfg queue-name xml)
          (if (= :STR0046R2 response-type)
            (store.messages/update-message! store (:id msg)
                                            #(update % :responses (fnil conj [])
                                                     {:type    response-type
                                                      :body    xml
                                                      :sent-at (str (Instant/now))}))
            (store.messages/update-message! store (:id msg)
                                            #(-> %
                                                 (assoc :status :responded)
                                                 (update :responses (fnil conj [])
                                                         {:type         response-type
                                                          :body         xml
                                                          :sent-at      (str (Instant/now))
                                                          :num-ctrl-str (:NumCtrlSTR fields)}))))
          {:ok true})))))
