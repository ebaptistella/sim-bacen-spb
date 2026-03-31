(ns com.github.ebaptistella.infrastructure.mq.producer
  "IBM MQ producer. Sends messages to configured response queue.
   Queue name must be provided by caller (resolved from env var at startup)."
  (:require [schema.core :as s])
  (:import (com.ibm.mq MQException MQMessage MQPutMessageOptions MQQueueManager)
           (com.ibm.mq.constants CMQC)))

;; IBM MQ reason code: queue name not found in queue manager.
(def ^:private mqrc-unknown-object-name 2085)

(s/defn ^:private build-queue-manager [mq-cfg]
  (let [props (doto (java.util.Hashtable.)
                (.put "hostname" (:host mq-cfg))
                (.put "port" (int (:port mq-cfg)))
                (.put "channel" (:channel mq-cfg))
                (.put "userID" (:user mq-cfg))
                (.put "password" (:password mq-cfg)))]
    (MQQueueManager. (:qmgr mq-cfg) props)))

(s/defn send-message!
  "Sends a message body to the configured queue (typically response queue).
   Queue name must be provided by caller (resolved from IBMMQ_QL_RSP_NAME env var at startup).

   Queue separation: producer is decoupled from queue hardcoding. The response queue
   (IBMMQ_QL_RSP_NAME) is where producer sends reply messages and autonomous SLB
   messages. The request queue (IBMMQ_QL_REQ_NAME) is exclusively consumed by
   the MQ worker and populated by test ingest endpoints.

   If the destination queue does not exist (MQRC 2085), logs a warning and
   returns normally — this is best-effort delivery for the simulator.

   Returns true on success, false if queue not found."
  [mq-cfg queue-name body]
  (try
    (let [qmgr  (build-queue-manager mq-cfg)
          queue (.accessQueue qmgr queue-name (bit-or CMQC/MQOO_OUTPUT CMQC/MQOO_FAIL_IF_QUIESCING))
          msg   (doto (MQMessage.) (.writeUTF body))
          msg-id (String. (.messageId msg))]
      (.put queue msg (MQPutMessageOptions.))
      (.close queue)
      (.disconnect qmgr)
      true)
    (catch MQException e
      (if (= mqrc-unknown-object-name (.getReason e))
        (do
          (println (str "[MQProducer] WARN Queue not found (MQRC 2085): " queue-name
                        " | timestamp=" (System/currentTimeMillis)))
          false)
        (throw e)))))
