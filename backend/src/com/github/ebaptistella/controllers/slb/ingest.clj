(ns com.github.ebaptistella.controllers.slb.ingest
  "SLB message ingestion controller (HTTP → MQ + Store)."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.config.reader :as config.reader]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.slb.builder :as builder]
            [com.github.ebaptistella.logic.slb.correlation :as correlation]
            [schema.core :as s]
            [clojure.string :as str])
  (:import [java.time Instant]
           [java.util UUID]))

(s/defn send-slb-message! :- {s/Keyword s/Any}
  "Validate JSON schema, build XML, publish to MQ, store in atom."
  [msg-type :- s/Str
   data :- {s/Keyword s/Any}
   {:keys [store mq-cfg logger config] :as _components}]

  (try
    (let [builder-fn (builder/get-builder msg-type)]
      (if (nil? builder-fn)
        {:error :unsupported-type}
        (let [
              ; For request-response types, generate or use provided NumCtrlPart
              enriched-data (if (str/includes? msg-type "SLB0006")
                              (assoc data :NumCtrlPart (or (:NumCtrlPart data)
                                                           (correlation/generate-num-ctrl-part)))
                              (if (#{:SLB0002 :SLB0007} (keyword msg-type))
                                (assoc data :NumCtrlPart (correlation/generate-num-ctrl-part))
                                data))

              xml (builder-fn enriched-data)
              queue-name (config.reader/mq-response-queue-name config)
              msg-id (str (UUID/randomUUID))
              now (str (Instant/now))

              ; Build message record for store
              msg-record {:id              msg-id
                          :type            msg-type
                          :status          :pending
                          :direction       :outbound
                          :participant     (:ISPBPart enriched-data)
                          :queue-name      queue-name
                          :message-id      msg-id
                          :body            xml
                          :sent-at         now
                          :num-ctrl-part   (when (str/includes? msg-type "SLB00")
                                             (:NumCtrlPart enriched-data))
                          :num-ctrl-slb    (when (str/includes? msg-type "SLB0001")
                                             (:NumCtrlSLB enriched-data))}]

          (when logger
            (logger/log-call logger :info "[SLB] Injecting %s | id=%s type=%s"
                            msg-type msg-id msg-type))

          ; Publish to MQ
          (mq.producer/send-message! mq-cfg queue-name xml)

          ; Store in atom
          (store.messages/save! store msg-record)

          {:ok true
           :id msg-id
           :type msg-type
           :num-ctrl-part (:NumCtrlPart enriched-data)
           :sent-at now})))

    (catch Exception e
      (when logger
        (logger/log-call logger :error "[SLB] Ingest failed | type=%s error=%s"
                        msg-type (.getMessage e)))
      {:error :mq-or-store-error
       :message (.getMessage e)})))
