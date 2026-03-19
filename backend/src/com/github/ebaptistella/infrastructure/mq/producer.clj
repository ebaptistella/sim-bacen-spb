(ns com.github.ebaptistella.infrastructure.mq.producer
  "IBM MQ producer. Sends messages to outbound SPB queues (QR.*)."
  (:require [schema.core :as s])
  (:import (com.ibm.mq MQMessage MQPutMessageOptions MQQueueManager)
           (com.ibm.mq.constants CMQC)))

(s/defn ^:private build-queue-manager [mq-cfg]
  (let [props (doto (java.util.Hashtable.)
                (.put "hostname" (:host mq-cfg))
                (.put "port" (int (:port mq-cfg)))
                (.put "channel" (:channel mq-cfg))
                (.put "userID" (:user mq-cfg))
                (.put "password" (:password mq-cfg)))]
    (MQQueueManager. (:qmgr mq-cfg) props)))

(s/defn send-message!
  "Sends a message body to the given outbound queue (QR.*)."
  [mq-cfg queue-name body]
  (let [qmgr  (build-queue-manager mq-cfg)
        queue (.accessQueue qmgr queue-name (bit-or CMQC/MQOO_OUTPUT CMQC/MQOO_FAIL_IF_QUIESCING))
        msg   (doto (MQMessage.) (.writeUTF body))]
    (.put queue msg (MQPutMessageOptions.))
    (.close queue)
    (.disconnect qmgr)))
