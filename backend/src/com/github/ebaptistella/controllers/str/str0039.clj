(ns com.github.ebaptistella.controllers.str.str0039
  "STR0039: Transferência para portabilidade de crédito PJ. Fluxo2: R1 + R2 + E."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.controllers.str.str :refer [available-responses process! respond!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0039 :as logic.str0039])
  (:import [java.time Instant]))

(def ^:private accepted-response-types #{:STR0039R1 :STR0039R2 :STR0039E})

(defmethod available-responses :STR0039 [_msg] [:STR0039R1 :STR0039R2 :STR0039E])

(defmethod process! :STR0039
  [msg {:keys [store logger]}]
  (logger/log-call logger :info "[STR] STR0039 received | id=%s type=%s"
                   (:message-id msg) (:type msg))
  (store.messages/save! store msg))

(defmethod respond! :STR0039
  [msg {:keys [store mq-cfg]} {:keys [response-type params]}]
  (cond
    (and (= :responded (:status msg)) (not= :STR0039R2 response-type))
    {:error :already-responded}

    (not (accepted-response-types response-type))
    {:error :invalid-response-type}

    (and (= :responded (:status msg)) (= :STR0039R2 response-type) (store.messages/response-sent? msg :STR0039R2))
    {:error :r2-already-sent}

    (and (= :pending (:status msg)) (= :STR0039R2 response-type))
    {:error :r2-requires-r1}

    :else
    (let [p      (cond-> (or params {})
                   (= :STR0039R2 response-type)
                   (assoc :NumCtrlSTR (-> msg :responses first :num-ctrl-str)))
          fields (case response-type
                   :STR0039R1 (logic.str0039/r1-response msg p)
                   :STR0039R2 (logic.str0039/r2-response msg p)
                   :STR0039E  (logic.str0039/rejection-response msg p))]
      (if (= :missing-motivo (:error fields))
        fields
        (let [xml        (logic.str0039/response->xml response-type fields)
              queue-name (cond
                           (#{:STR0039R1 :STR0039E} response-type)
                           (parser/r1-outbound-queue (:queue-name msg))

                           (= :STR0039R2 response-type)
                           (parser/r2-outbound-queue (:queue-name msg) (:ispb-if-credtd msg)))]
          (when (nil? queue-name)
            (throw (ex-info "Cannot derive outbound queue for STR0039R2 (missing ISPBIFCredtd?)" {})))
          (mq.producer/send-message! mq-cfg queue-name xml)
          (if (= :STR0039R2 response-type)
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
