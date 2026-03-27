(ns com.github.ebaptistella.controllers.str.str0048
  "STR0048: devolução de portabilidade indevida. R1, R2 (IF-Creditada), R3 (IF-Devedora), E (rejeição)."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.controllers.str.str :refer [available-responses process! respond!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0048 :as logic.str0048])
  (:import [java.time Instant]))

(def ^:private accepted-response-types #{:STR0048R1 :STR0048R2 :STR0048R3 :STR0048E})

(defmethod available-responses :STR0048
  [msg]
  (let [responses (:responses msg)]
    (cond
      (some #(= :STR0048R3 (:type %)) responses) []
      (some #(= :STR0048E  (:type %)) responses) []
      (some #(= :STR0048R2 (:type %)) responses) [:STR0048R3]
      (some #(= :STR0048R1 (:type %)) responses) [:STR0048R2]
      :else                                       [:STR0048R1 :STR0048R2 :STR0048E])))

(defmethod process! :STR0048
  [msg {:keys [store logger]}]
  (logger/log-call logger :info "[STR] STR0048 received | id=%s num-ctrl-str-or=%s"
                   (:message-id msg) (:num-ctrl-str-or msg))
  (store.messages/save! store msg))

(defmethod respond! :STR0048
  [msg {:keys [store mq-cfg]} {:keys [response-type params]}]
  (let [responses  (:responses msg)
        r1-sent?   (some #(= :STR0048R1 (:type %)) responses)
        r2-sent?   (some #(= :STR0048R2 (:type %)) responses)
        r3-sent?   (some #(= :STR0048R3 (:type %)) responses)]
    (cond
      (not (accepted-response-types response-type))
      {:error :invalid-response-type}

      (and (= :STR0048R1 response-type) r1-sent?)
      {:error :already-responded}

      (and (= :STR0048R1 response-type) (store.messages/response-sent? msg :STR0048E))
      {:error :already-responded}

      (and (#{:STR0048R2 :STR0048R3} response-type) (not r1-sent?))
      {:error :r2-requires-r1}

      (and (= :STR0048R3 response-type) (not r2-sent?))
      {:error :r3-requires-r2}

      (and (= :STR0048R2 response-type) r2-sent?)
      {:error :r2-already-sent}

      (and (= :STR0048R3 response-type) r3-sent?)
      {:error :r3-already-sent}

      (and (= :STR0048E response-type) r1-sent?)
      {:error :already-responded}

      :else
      (let [p      (cond-> (or params {})
                     (#{:STR0048R2 :STR0048R3} response-type)
                     (assoc :NumCtrlSTR (-> responses first :num-ctrl-str)))
            fields (case response-type
                     :STR0048R1 (logic.str0048/r1-response msg p)
                     :STR0048R2 (logic.str0048/r2-response msg p)
                     :STR0048R3 (logic.str0048/r3-response msg p)
                     :STR0048E  (logic.str0048/rejection-response msg p))]
        (if (= :missing-motivo (:error fields))
          fields
          (let [ispb-devedora (or (:ispb-if-devedora msg) (:ISPBIFDevedora (or params {})))
                xml           (logic.str0048/response->xml response-type fields)
                queue-name    (cond
                                (#{:STR0048R1 :STR0048E} response-type)
                                (parser/r1-outbound-queue (:queue-name msg))

                                (= :STR0048R2 response-type)
                                (parser/r2-outbound-queue (:queue-name msg) (:ispb-if-credtd msg))

                                (= :STR0048R3 response-type)
                                (parser/r2-outbound-queue (:queue-name msg) ispb-devedora))]
            (when (nil? queue-name)
              (throw (ex-info "Cannot derive outbound queue: missing ISPB for this response type" {})))
            (mq.producer/send-message! mq-cfg queue-name xml)
            (if (#{:STR0048R1 :STR0048E} response-type)
              (store.messages/update-message! store (:id msg)
                                              #(-> %
                                                   (assoc :status :responded)
                                                   (update :responses (fnil conj [])
                                                           (cond-> {:type    response-type
                                                                    :body    xml
                                                                    :sent-at (str (Instant/now))}
                                                             (= :STR0048R1 response-type)
                                                             (assoc :num-ctrl-str (:NumCtrlSTR fields))))))
              (store.messages/update-message! store (:id msg)
                                              #(update % :responses (fnil conj [])
                                                       {:type    response-type
                                                        :body    xml
                                                        :sent-at (str (Instant/now))})))
            {:ok true}))))))
