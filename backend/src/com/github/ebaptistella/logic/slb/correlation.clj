(ns com.github.ebaptistella.logic.slb.correlation
  "Correlation ID generation and management for SLB request-response messages."
  (:require [schema.core :as s]
            [clojure.string :as str])
  (:import [java.util UUID]))

(s/defn generate-num-ctrl-part :- s/Str
  "Generate a unique NumCtrlPart for request-response SLB messages (SLB0002, SLB0006, SLB0007)."
  []
  (str "SLB-" (str/upper-case (subs (str (UUID/randomUUID)) 0 8))))
