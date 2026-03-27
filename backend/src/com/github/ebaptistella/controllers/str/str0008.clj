(ns com.github.ebaptistella.controllers.str.str0008
  "STR0008: inbound persistence and response orchestration (Logic Sandwich)."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.controllers.str.str :refer [process! respond!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0008 :as logic.str0008])
  (:import [java.time Instant]))

(def ^:private accepted-response-types #{:STR0008R1 :STR0008R2 :STR0008E})

(defmethod process! :STR0008
  [msg {:keys [store logger]}]
  (logger/log-call logger :info "[STR] STR0008 received | id=%s type=%s"
                   (:message-id msg) (:type msg))
  (store.messages/save! store msg))

(defmethod respond! :STR0008
  [msg {:keys [store mq-cfg]} {:keys [response-type params]}]
  (cond
    (and (= :responded (:status msg)) (not= :STR0008R2 response-type))
    {:error :already-responded}

    (not (accepted-response-types response-type))
    {:error :invalid-response-type}

    (and (= :responded (:status msg)) (= :STR0008R2 response-type) (:r2-response msg))
    {:error :r2-already-sent}

    (and (= :pending (:status msg)) (= :STR0008R2 response-type))
    {:error :r2-requires-r1}

    :else
    (let [p      (cond-> (or params {})
                   (= :STR0008R2 response-type)
                   (assoc :NumCtrlSTR (get-in msg [:response :num-ctrl-str])))
          fields (case response-type
                   :STR0008R1 (logic.str0008/r1-response msg p)
                   :STR0008R2 (logic.str0008/r2-response msg p)
                   :STR0008E  (logic.str0008/rejection-response msg p))]
      (if (= :missing-motivo (:error fields))
        fields
        (let [xml        (logic.str0008/response->xml response-type fields)
              queue-name (cond
                           (#{:STR0008R1 :STR0008E} response-type)
                           (parser/r1-outbound-queue (:queue-name msg))

                           (= :STR0008R2 response-type)
                           (parser/r2-outbound-queue (:queue-name msg) (:ispb-if-credtd msg)))]
          (when (nil? queue-name)
            (throw (ex-info "Cannot derive outbound queue for STR0008R2 (missing ISPBIFCredtd?)" {})))
          (mq.producer/send-message! mq-cfg queue-name xml)
          (if (= :STR0008R2 response-type)
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
