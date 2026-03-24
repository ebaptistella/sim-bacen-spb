(ns com.github.ebaptistella.infrastructure.mq.consumer
  "IBM MQ consumer. Reads messages from inbound SPB queues (QL.*)."
  (:require [schema.core :as s])
  (:import (com.ibm.mq MQException MQGetMessageOptions MQMessage MQQueueManager)
           (com.ibm.mq.constants CMQC)))

(s/defn ^:private build-queue-manager [mq-cfg]
  (let [props (doto (java.util.Hashtable.)
                (.put "hostname" (:host mq-cfg))
                (.put "port" (int (:port mq-cfg)))
                (.put "channel" (:channel mq-cfg))
                (.put "userID" (:user mq-cfg))
                (.put "password" (:password mq-cfg)))]
    (MQQueueManager. (:qmgr mq-cfg) props)))

(s/defn ^:private make-gmo
  "Creates MQGetMessageOptions configured for non-blocking get."
  []
  (let [gmo (MQGetMessageOptions.)]
    (set! (.-options gmo) CMQC/MQGMO_NO_WAIT)
    gmo))

(s/defn receive-messages
  "Reads up to `limit` messages from the inbound queue (QL.REQ.*).
   Returns a sequence of maps with :queue-name, :message-id, :body."
  [mq-cfg limit]
  (let [queue-name (System/getenv "IBMMQ_QL_REQ_NAME")
        qmgr       (build-queue-manager mq-cfg)
        open-opts  (bit-or CMQC/MQOO_INPUT_AS_Q_DEF CMQC/MQOO_FAIL_IF_QUIESCING)
        queue      (.accessQueue qmgr queue-name open-opts)
        gmo        (make-gmo)]
    (try
      (loop [messages [] n 0]
        (if (>= n limit)
          messages
          (let [result (try
                         (let [msg (MQMessage.)]
                           (.get queue msg gmo)
                           {:queue-name queue-name
                            :message-id (String. (.messageId msg))
                            :body       (.readString msg (.getMessageLength msg))})
                         (catch MQException e
                           (if (= (.getReason e) CMQC/MQRC_NO_MSG_AVAILABLE)
                             ::no-more-messages
                             (throw e))))]
            (if (= result ::no-more-messages)
              messages
              (recur (conj messages result) (inc n))))))
      (catch Exception e
        [])
      (finally
        (try (.close queue) (catch Exception _))
        (try (.disconnect qmgr) (catch Exception _))))))
