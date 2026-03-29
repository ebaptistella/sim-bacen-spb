(ns com.github.ebaptistella.wire.in.unified-parser
  "Unified parser for both STR and SLB messages from MQ."
  (:require [clojure.string :as str]
            [com.github.ebaptistella.wire.in.str.str :as str-parser]
            [com.github.ebaptistella.wire.in.slb.str :as slb-parser]))

(def ^:private slb-types #{"SLB0001" "SLB0002" "SLB0003" "SLB0005" "SLB0006" "SLB0007" "SLB0008"
                           "SLB0002R1" "SLB0006R1" "SLB0007R1"})

(defn- extract-cod-msg [body]
  (when-not (str/blank? body)
    (second (re-find #"<CodMsg>([A-Z0-9]+)</CodMsg>" body))))

(defn parse-inbound
  "Parse inbound message as either STR or SLB based on CodMsg."
  [msg]
  (let [body (or (:body msg) "")
        cod-msg (extract-cod-msg body)]
    (cond
      (and cod-msg (slb-types cod-msg))
      (if (str/ends-with? cod-msg "R1")
        (slb-parser/parse-response msg)
        (slb-parser/parse-inbound msg))

      :else
      (str-parser/parse-inbound msg))))
