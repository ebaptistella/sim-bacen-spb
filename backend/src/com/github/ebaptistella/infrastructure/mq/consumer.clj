(ns com.github.ebaptistella.infrastructure.mq.consumer
  "IBM MQ consumer. Reads messages from inbound SPB queues (QL.*)."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [schema.core :as s])
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
  "Reads up to `limit` messages from the configured request queue.
   Accepts request-queue-name as parameter (resolved from env var at startup).
   Optional `log` is a bound logger from `logger/bound` (pass nil to skip logging).

   Queue separation: consumer is hardcoded to read only from the request queue
   (IBMMQ_QL_REQ_NAME), ensuring requests from the IF are processed by the
   normal message pipeline. Response/autonomous messages are published to
   a separate response queue (IBMMQ_QL_RSP_NAME) and not consumed here."
  [mq-cfg request-queue-name limit log]
  (try
    (let [queue-name request-queue-name
          qmgr       (try
                       (build-queue-manager mq-cfg)
                       (catch Exception e
                         (logger/log-call log :error "[MQConsumer] Failed to create queue manager | %s"
                                          (.getMessage e))
                         (throw e)))
          open-opts  (bit-or CMQC/MQOO_INPUT_AS_Q_DEF CMQC/MQOO_FAIL_IF_QUIESCING)
          queue      (try
                       (.accessQueue qmgr queue-name open-opts)
                       (catch Exception e
                         (logger/log-call log :error "[MQConsumer] Failed to access queue %s | %s"
                                          queue-name (.getMessage e))
                         (throw e)))
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
          (logger/log-call log :warn "[MQConsumer] Failed to receive messages from %s | %s"
                           queue-name (.getMessage e))
          [])
        (finally
          (try (.close queue) (catch Exception _))
          (try (.disconnect qmgr) (catch Exception _)))))
    (catch Exception e
      (logger/log-call log :error "[MQConsumer] Unexpected error in receive-messages | %s"
                       (.getMessage e))
      [])))
