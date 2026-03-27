(ns com.github.ebaptistella.controllers.str.str0005
  "STR0005: TED por não-correntista. Fluxo2: R1 + R2 + E."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.controllers.str.str :refer [available-responses process! respond!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0005 :as logic.str0005])
  (:import [java.time Instant]))

(def ^:private accepted-response-types #{:STR0005R1 :STR0005R2 :STR0005E})

(defmethod available-responses :STR0005 [_msg] [:STR0005R1 :STR0005R2 :STR0005E])

(defmethod process! :STR0005
  [msg {:keys [store logger]}]
  (logger/log-call logger :info "[STR] STR0005 received | id=%s type=%s"
                   (:message-id msg) (:type msg))
  (store.messages/save! store msg))

(defmethod respond! :STR0005
  [msg {:keys [store mq-cfg]} {:keys [response-type params]}]
  (cond
    (and (= :responded (:status msg)) (not= :STR0005R2 response-type))
    {:error :already-responded}

    (not (accepted-response-types response-type))
    {:error :invalid-response-type}

    (and (= :responded (:status msg)) (= :STR0005R2 response-type) (store.messages/response-sent? msg :STR0005R2))
    {:error :r2-already-sent}

    (and (= :pending (:status msg)) (= :STR0005R2 response-type))
    {:error :r2-requires-r1}

    :else
    (let [p      (cond-> (or params {})
                   (= :STR0005R2 response-type)
                   (assoc :NumCtrlSTR (-> msg :responses first :num-ctrl-str)))
          fields (case response-type
                   :STR0005R1 (logic.str0005/r1-response msg p)
                   :STR0005R2 (logic.str0005/r2-response msg p)
                   :STR0005E  (logic.str0005/rejection-response msg p))]
      (if (= :missing-motivo (:error fields))
        fields
        (let [xml        (logic.str0005/response->xml response-type fields)
              queue-name (cond
                           (#{:STR0005R1 :STR0005E} response-type)
                           (parser/r1-outbound-queue (:queue-name msg))

                           (= :STR0005R2 response-type)
                           (parser/r2-outbound-queue (:queue-name msg) (:ispb-if-credtd msg)))]
          (when (nil? queue-name)
            (throw (ex-info "Cannot derive outbound queue for STR0005R2 (missing ISPBIFCredtd?)" {})))
          (mq.producer/send-message! mq-cfg queue-name xml)
          (if (= :STR0005R2 response-type)
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
