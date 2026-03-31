(ns com.github.ebaptistella.config.reader
  (:require [com.github.ebaptistella.components.configuration :as components.configuration]
            [schema.core :as s]))

(def default-config-file
  "config/application.edn")

(def default-application-name
  "sim-bacen-spb")

(def ^:private default-mq-worker
  {:poll-interval-ms 2000
   :thread-pool-size 4
   :batch-limit      10})

(s/defn ^:private get-config
  [config-component]
  (components.configuration/get-config config-component))

(s/defn ^:private get-http-config
  [config-component]
  (get-in (get-config config-component) [:http]))

(s/defn http->port
  [config-component]
  (:port (get-http-config config-component)))

(s/defn http->host
  [config-component]
  (:host (get-http-config config-component)))

(s/defn mq-config
  "Returns the IBM MQ config from the config component.
   Applies environment variable overrides."
  [config-component]
  (let [base (get-in (get-config config-component) [:mq])]
    (merge base
           {:host     (or (System/getenv "IBMMQ_HOST") (:host base))
            :port     (or (some-> (System/getenv "IBMMQ_PORT") Integer/parseInt) (:port base))
            :channel  (or (System/getenv "IBMMQ_CHANNEL") (:channel base))
            :qmgr     (or (System/getenv "IBMMQ_QMGR_NAME") (:qmgr base))
            :user     (or (System/getenv "IBMMQ_USER") (:user base))
            :password (or (System/getenv "IBMMQ_PASSWORD") (:password base))})))

(s/defn simulator-ispb
  "Returns the ISPB of this simulator instance.
   Overridable via SIMULATOR_ISPB env var."
  [config-component]
  (or (System/getenv "SIMULATOR_ISPB")
      (get-in (get-config config-component) [:simulator :ispb])
      "99999999"))

(s/defn mq-worker-config
  "Returns mq-worker config from the config component.
   Merges with defaults for :poll-interval-ms, :thread-pool-size, :batch-limit."
  [config-component]
  (merge default-mq-worker
         (get-in (get-config config-component) [:mq-worker])))

(s/defn str-horario-abertura
  "Returns the STR opening hour. Overridable via STR_HORARIO_ABERTURA env var."
  [config-component]
  (or (System/getenv "STR_HORARIO_ABERTURA")
      (get-in (get-config config-component) [:str :horario-abertura])
      "07:00"))

(s/defn str-horario-fechamento
  "Returns the STR closing hour. Overridable via STR_HORARIO_FECHAMENTO env var."
  [config-component]
  (or (System/getenv "STR_HORARIO_FECHAMENTO")
      (get-in (get-config config-component) [:str :horario-fechamento])
      "17:30"))

(s/defn str-saldo-simulado
  "Returns the simulated account balance. Overridable via STR_SALDO_SIMULADO env var."
  [config-component]
  (or (System/getenv "STR_SALDO_SIMULADO")
      (get-in (get-config config-component) [:str :saldo-simulado])
      "99999999.99"))

(s/defn ^:private resolve-queue-name
  "Helper function to safely read queue name from environment variable.
   Returns the env var value if set, otherwise nil.
   Logging should be done at call site."
  [env-var-name]
  (System/getenv env-var-name))

(s/defn mq-request-queue-name
  "Returns the IBM MQ request queue name (inbound from IF).
   Resolved from IBMMQ_QL_REQ_NAME environment variable at startup.
   If not set, returns nil and caller should log a warning."
  [config-component]
  (resolve-queue-name "IBMMQ_QL_REQ_NAME"))

(s/defn mq-response-queue-name
  "Returns the IBM MQ response queue name (outbound to BACEN).
   Resolved from IBMMQ_QL_RSP_NAME environment variable at startup.
   If not set, returns nil and caller should log a warning."
  [config-component]
  (resolve-queue-name "IBMMQ_QL_RSP_NAME"))
