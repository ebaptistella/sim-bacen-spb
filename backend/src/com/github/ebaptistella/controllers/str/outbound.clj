(ns com.github.ebaptistella.controllers.str.outbound
  "Outbound broadcast controller: builds and sends STR0015/16/17/18/19/30/42/50 on operator request."
  (:require [com.github.ebaptistella.config.reader :as config.reader]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.str0015 :as logic.str0015]
            [com.github.ebaptistella.logic.str.str0016 :as logic.str0016]
            [com.github.ebaptistella.logic.str.str0017 :as logic.str0017]
            [com.github.ebaptistella.logic.str.str0018 :as logic.str0018]
            [com.github.ebaptistella.logic.str.str0019 :as logic.str0019]
            [com.github.ebaptistella.logic.str.str0030 :as logic.str0030]
            [com.github.ebaptistella.logic.str.str0042 :as logic.str0042]
            [com.github.ebaptistella.logic.str.str0050 :as logic.str0050]
            [schema.core :as s])
  (:import [java.time Instant]
           [java.util UUID]))

(def ^:private valid-types #{"STR0015" "STR0016" "STR0017" "STR0018" "STR0019" "STR0030" "STR0042" "STR0050"})
(def ^:private participant-pattern #"[0-9]{8}")

(defn- build-config-map [config-component]
  {:str-horario-abertura   (config.reader/str-horario-abertura config-component)
   :str-horario-fechamento (config.reader/str-horario-fechamento config-component)
   :str-saldo-simulado     (config.reader/str-saldo-simulado config-component)
   :simulator-ispb         (config.reader/simulator-ispb config-component)})

(s/defn send-outbound!
  "Validates, builds, sends and stores an outbound broadcast message.
   Returns {:ok true :id ... :sent-at ...} or {:error :invalid-type/:invalid-participant/:mq-error}."
  [msg-type :- s/Str
   participant :- s/Str
   params :- {s/Any s/Any}
   {:keys [store mq-cfg config]}]
  (cond
    (not (valid-types msg-type))
    {:error :invalid-type}

    (not (re-matches participant-pattern participant))
    {:error :invalid-participant}

    :else
    (let [cfg            (build-config-map config)
          simulator-ispb (:simulator-ispb cfg)
          xml            (case msg-type
                           "STR0015" (logic.str0015/build-message params cfg)
                           "STR0016" (logic.str0016/build-message params participant cfg)
                           "STR0017" (logic.str0017/build-message params cfg)
                           "STR0018" (logic.str0018/build-message params cfg)
                           "STR0019" (logic.str0019/build-message params cfg)
                           "STR0030" (logic.str0030/build-message params cfg)
                           "STR0042" (logic.str0042/build-message params cfg)
                           "STR0050" (logic.str0050/build-message params cfg))
          queue          (case msg-type
                           "STR0015" (logic.str0015/queue-name simulator-ispb participant)
                           "STR0016" (logic.str0016/queue-name simulator-ispb participant)
                           "STR0017" (logic.str0017/queue-name simulator-ispb participant)
                           "STR0018" (logic.str0018/queue-name simulator-ispb participant)
                           "STR0019" (logic.str0019/queue-name simulator-ispb participant)
                           "STR0030" (logic.str0030/queue-name simulator-ispb participant)
                           "STR0042" (logic.str0042/queue-name simulator-ispb participant)
                           "STR0050" (logic.str0050/queue-name simulator-ispb participant))]
      (try
        (mq.producer/send-message! mq-cfg queue xml)
        (let [id      (str (UUID/randomUUID))
              sent-at (str (Instant/now))
              entry   {:id          id
                       :type        msg-type
                       :direction   :outbound
                       :status      :sent
                       :participant participant
                       :queue-name  queue
                       :body        xml
                       :sent-at     sent-at}]
          (store.messages/save! store entry)
          {:ok true :id id :sent-at sent-at})
        (catch Exception _
          {:error :mq-error})))))
