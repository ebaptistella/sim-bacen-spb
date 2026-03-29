(ns com.github.ebaptistella.controllers.slb.response
  "SLB response message processing (SLB0002R1, SLB0006R1, SLB0007R1)."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]))

(defn process-slb-response!
  "Process inbound SLB response message (SLB00XXR1) and save to store."
  [msg {:keys [store logger]}]
  (when logger
    (logger/log-call logger :info "[SLB] Processing response | type=%s num-ctrl-part=%s"
                    (:type msg) (:num-ctrl-part msg)))
  (store.messages/save! store msg))
