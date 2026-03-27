(ns com.github.ebaptistella.controllers.str.str0007
  "STR0007: TED de IF para conta de cliente. Fluxo2: R1 + R2 (FinlddIF) + E."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.controllers.str.str :refer [process! respond!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0007 :as logic.str0007])
  (:import [java.time Instant]))

(def ^:private accepted-response-types #{:STR0007R1 :STR0007R2 :STR0007E})

(defmethod process! :STR0007
  [msg {:keys [store logger]}]
  (logger/log-call logger :info "[STR] STR0007 received | id=%s type=%s"
                   (:message-id msg) (:type msg))
  (store.messages/save! store msg))

(defmethod respond! :STR0007
  [msg {:keys [store mq-cfg]} {:keys [response-type params]}]
  (cond
    (and (= :responded (:status msg)) (not= :STR0007R2 response-type))
    {:error :already-responded}

    (not (accepted-response-types response-type))
    {:error :invalid-response-type}

    (and (= :responded (:status msg)) (= :STR0007R2 response-type) (:r2-response msg))
    {:error :r2-already-sent}

    (and (= :pending (:status msg)) (= :STR0007R2 response-type))
    {:error :r2-requires-r1}

    :else
    (let [p      (cond-> (or params {})
                   (= :STR0007R2 response-type)
                   (assoc :NumCtrlSTR (get-in msg [:response :num-ctrl-str])))
          fields (case response-type
                   :STR0007R1 (logic.str0007/r1-response msg p)
                   :STR0007R2 (logic.str0007/r2-response msg p)
                   :STR0007E  (logic.str0007/rejection-response msg p))]
      (if (= :missing-motivo (:error fields))
        fields
        (let [xml        (logic.str0007/response->xml response-type fields)
              queue-name (cond
                           (#{:STR0007R1 :STR0007E} response-type)
                           (parser/r1-outbound-queue (:queue-name msg))

                           (= :STR0007R2 response-type)
                           (parser/r2-outbound-queue (:queue-name msg) (:ispb-if-credtd msg)))]
          (when (nil? queue-name)
            (throw (ex-info "Cannot derive outbound queue for STR0007R2 (missing ISPBIFCredtd?)" {})))
          (mq.producer/send-message! mq-cfg queue-name xml)
          (if (= :STR0007R2 response-type)
            (store.messages/update-message! store (:id msg)
                                            #(assoc % :r2-response {:type response-type
                                                                     :body xml
                                                                     :sent-at (str (Instant/now))}))
            (store.messages/update-message! store (:id msg)
                                            #(assoc % :status :responded
                                                      :response {:type         response-type
                                                                 :body         xml
                                                                 :sent-at      (str (Instant/now))
                                                                 :num-ctrl-str (:NumCtrlSTR fields)})))
          {:ok true})))))
