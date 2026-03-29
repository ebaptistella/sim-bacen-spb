(ns com.github.ebaptistella.infrastructure.mq.producer
  "IBM MQ producer. Sends messages to outbound SPB queues (QR.*)."
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
  "Sends a message body to the given outbound queue (QR.*).
   If the destination queue does not exist (MQRC 2085), logs a warning and
   returns normally — this is best-effort delivery for the simulator."
  [mq-cfg queue-name body]
  (try
    (let [qmgr  (build-queue-manager mq-cfg)
          queue (.accessQueue qmgr queue-name (bit-or CMQC/MQOO_OUTPUT CMQC/MQOO_FAIL_IF_QUIESCING))
          msg   (doto (MQMessage.) (.writeUTF body))]
      (.put queue msg (MQPutMessageOptions.))
      (.close queue)
      (.disconnect qmgr))
    (catch MQException e
      (if (= mqrc-unknown-object-name (.getReason e))
        (println (str "[MQProducer] Queue not found (skipped): " queue-name))
        (throw e)))))
