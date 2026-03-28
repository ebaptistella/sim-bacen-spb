(ns com.github.ebaptistella.controllers.str.str0020
  "STR0020: Repasse de tributos estaduais. Fluxo2: R1 + R2 + E."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.controllers.str.str :refer [available-responses process! respond!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0020 :as logic.str0020])
  (:import [java.time Instant]))

(def ^:private accepted-response-types #{:STR0020R1 :STR0020R2 :STR0020E})

(defmethod available-responses :STR0020 [_msg] [:STR0020R1 :STR0020R2 :STR0020E])

(defmethod process! :STR0020
  [msg {:keys [store logger]}]
  (logger/log-call logger :info "[STR] STR0020 received | id=%s type=%s"
                   (:message-id msg) (:type msg))
  (store.messages/save! store msg))

(defmethod respond! :STR0020
  [msg {:keys [store mq-cfg]} {:keys [response-type params]}]
  (cond
    (and (= :responded (:status msg)) (not= :STR0020R2 response-type))
    {:error :already-responded}

    (not (accepted-response-types response-type))
    {:error :invalid-response-type}

    (and (= :responded (:status msg)) (= :STR0020R2 response-type) (store.messages/response-sent? msg :STR0020R2))
    {:error :r2-already-sent}

    (and (= :pending (:status msg)) (= :STR0020R2 response-type))
    {:error :r2-requires-r1}

    :else
    (let [p      (cond-> (or params {})
                   (= :STR0020R2 response-type)
                   (assoc :NumCtrlSTR (-> msg :responses first :num-ctrl-str)))
          fields (case response-type
                   :STR0020R1 (logic.str0020/r1-response msg p)
                   :STR0020R2 (logic.str0020/r2-response msg p)
                   :STR0020E  (logic.str0020/rejection-response msg p))]
      (if (= :missing-motivo (:error fields))
        fields
        (let [xml        (logic.str0020/response->xml response-type fields)
              queue-name (cond
                           (#{:STR0020R1 :STR0020E} response-type)
                           (parser/r1-outbound-queue (:queue-name msg))

                           (= :STR0020R2 response-type)
                           (parser/r2-outbound-queue (:queue-name msg) (:ispb-if-credtd msg)))]
          (when (nil? queue-name)
            (throw (ex-info "Cannot derive outbound queue for STR0020R2 (missing ISPBIFCredtd?)" {})))
          (mq.producer/send-message! mq-cfg queue-name xml)
          (if (= :STR0020R2 response-type)
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
