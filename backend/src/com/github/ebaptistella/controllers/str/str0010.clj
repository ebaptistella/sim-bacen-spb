(ns com.github.ebaptistella.controllers.str.str0010
  "STR0010: devolução de TED indevida. R1 (confirmação), R2 (notificação IF-Creditada), E (rejeição)."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.controllers.str.str :refer [available-responses process! respond!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0010 :as logic.str0010])
  (:import [java.time Instant]))

(def ^:private accepted-response-types #{:STR0010R1 :STR0010R2 :STR0010E})

(defmethod available-responses :STR0010
  [msg]
  (cond
    (some #(= :STR0010R2 (:type %)) (:responses msg)) []
    (some #(= :STR0010R1 (:type %)) (:responses msg)) [:STR0010R2]
    :else                                              [:STR0010R1 :STR0010R2 :STR0010E]))

(defmethod process! :STR0010
  [msg {:keys [store logger]}]
  (logger/log-call logger :info "[STR] STR0010 received | id=%s num-ctrl-str-or=%s"
                   (:message-id msg) (:num-ctrl-str-or msg))
  (store.messages/save! store msg))

(defmethod respond! :STR0010
  [msg {:keys [store mq-cfg]} {:keys [response-type params]}]
  (cond
    (and (= :responded (:status msg)) (not= :STR0010R2 response-type))
    {:error :already-responded}

    (not (accepted-response-types response-type))
    {:error :invalid-response-type}

    (and (= :responded (:status msg)) (= :STR0010R2 response-type) (store.messages/response-sent? msg :STR0010R2))
    {:error :r2-already-sent}

    (and (= :pending (:status msg)) (= :STR0010R2 response-type))
    {:error :r2-requires-r1}

    :else
    (let [p      (cond-> (or params {})
                   (= :STR0010R2 response-type)
                   (assoc :NumCtrlSTR (-> msg :responses first :num-ctrl-str)))
          fields (case response-type
                   :STR0010R1 (logic.str0010/r1-response msg p)
                   :STR0010R2 (logic.str0010/r2-response msg p)
                   :STR0010E  (logic.str0010/rejection-response msg p))]
      (if (= :missing-motivo (:error fields))
        fields
        (let [xml        (logic.str0010/response->xml response-type fields)
              queue-name (cond
                           (#{:STR0010R1 :STR0010E} response-type)
                           (parser/r1-outbound-queue (:queue-name msg))

                           (= :STR0010R2 response-type)
                           (parser/r2-outbound-queue (:queue-name msg) (:ispb-if-credtd msg)))]
          (when (nil? queue-name)
            (throw (ex-info "Cannot derive outbound queue for STR0010R2 (missing ISPBIFCredtd?)" {})))
          (mq.producer/send-message! mq-cfg queue-name xml)
          (if (= :STR0010R2 response-type)
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
