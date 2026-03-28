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

(def ^:private participant-pattern #"[0-9]{8}")

;; Each entry: {:build (fn [params participant cfg] xml) :queue (fn [simulator-ispb participant] queue-name)}
;; All build fns are normalised to the same arity (params participant cfg).
(def ^:private handlers
  {"STR0015" {:build (fn [p _ cfg] (logic.str0015/build-message p cfg))
              :queue logic.str0015/queue-name}
   "STR0016" {:build logic.str0016/build-message
              :queue logic.str0016/queue-name}
   "STR0017" {:build (fn [p _ cfg] (logic.str0017/build-message p cfg))
              :queue logic.str0017/queue-name}
   "STR0018" {:build (fn [p _ cfg] (logic.str0018/build-message p cfg))
              :queue logic.str0018/queue-name}
   "STR0019" {:build (fn [p _ cfg] (logic.str0019/build-message p cfg))
              :queue logic.str0019/queue-name}
   "STR0030" {:build (fn [p _ cfg] (logic.str0030/build-message p cfg))
              :queue logic.str0030/queue-name}
   "STR0042" {:build (fn [p _ cfg] (logic.str0042/build-message p cfg))
              :queue logic.str0042/queue-name}
   "STR0050" {:build (fn [p _ cfg] (logic.str0050/build-message p cfg))
              :queue logic.str0050/queue-name}})

(def ^:private valid-types (set (keys handlers)))

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
          handler        (get handlers msg-type)
          xml            ((:build handler) params participant cfg)
          queue          ((:queue handler) simulator-ispb participant)]
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
