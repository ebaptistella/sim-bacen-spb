(ns com.github.ebaptistella.components.mq-worker
  "IBM MQ Worker component. Consumes messages from inbound SPB queues (QL.*),
   processes them concurrently via a thread pool."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.config.reader :as config.reader]
            [com.github.ebaptistella.controllers.str.str :as controllers.str]
            com.github.ebaptistella.controllers.str.str0008
            com.github.ebaptistella.controllers.str.query
            [com.github.ebaptistella.infrastructure.mq.consumer :as mq.consumer]
            [com.github.ebaptistella.wire.in.str.str :as wire.in.str]
            com.github.ebaptistella.wire.in.str.str0008
            com.github.ebaptistella.wire.in.str.str0001
            com.github.ebaptistella.wire.in.str.str0012
            com.github.ebaptistella.wire.in.str.str0013
            com.github.ebaptistella.wire.in.str.str0014
            [com.stuartsierra.component :as component]
            [schema.core :as s])
  (:import [java.util.concurrent Executors TimeUnit]))

(defn- run-worker-loop [run? poll-ms limit mq-cfg store config log exec]
  (while @run?
    (try
      (Thread/sleep (long poll-ms))
      (when @run?
        (let [messages (mq.consumer/receive-messages mq-cfg limit)]
          (when (seq messages)
            (logger/log-call log :info "[MQWorker] Received %d message(s)" (count messages)))
          (doseq [raw messages]
            (.submit exec ^Runnable
                     (fn []
                       (try
                         (when-let [parsed (wire.in.str/parse-inbound raw)]
                           (controllers.str/process! parsed {:store  store
                                                             :logger log
                                                             :mq-cfg mq-cfg
                                                             :config config}))
                         (catch Exception e
                           (logger/log-call log :warn
                                            "[MQWorker] Failed to process message id=%s: %s"
                                            (:message-id raw) (.getMessage e)))))))))
      (catch InterruptedException _
        (reset! run? false))
      (catch Exception e
        (logger/log-call log :warn "[MQWorker] Poll loop error: %s" (.getMessage e))))))

(defrecord MQWorkerComponent [config logger store poll-interval-ms thread-pool-size batch-limit executor thread running?]
  component/Lifecycle
  (start [this]
    (if thread
      this
      (let [log        (logger/bound logger)
            worker-cfg (config.reader/mq-worker-config config)
            mq-cfg     (config.reader/mq-config config)
            poll-ms    (or poll-interval-ms (:poll-interval-ms worker-cfg))
            pool-size  (or thread-pool-size (:thread-pool-size worker-cfg))
            limit      (or batch-limit (:batch-limit worker-cfg))
            exec       (Executors/newFixedThreadPool (int pool-size))
            run?       (atom true)
            t          (doto (Thread. ^Runnable (fn [] (run-worker-loop run? poll-ms limit mq-cfg store config log exec)))
                         (.setName "mq-worker")
                         (.setDaemon true)
                         (.start))]
        (logger/log-call log :info "[MQWorker] Started | poll-ms=%d thread-pool-size=%d batch-limit=%d"
                         poll-ms pool-size limit)
        (assoc this :executor exec :thread t :running? run?
               :poll-interval-ms poll-ms :thread-pool-size pool-size :batch-limit limit))))

  (stop [this]
    (when logger
      (logger/log-call (logger/bound logger) :info "[MQWorker] Stopping"))
    (when running? (reset! running? false))
    (when thread (.interrupt thread) (.join thread 5000))
    (when executor (.shutdown executor) (.awaitTermination executor 10 TimeUnit/SECONDS))
    (dissoc this :executor :thread :running?)))

(s/defn new-mq-worker []
  (map->MQWorkerComponent {}))
