(ns com.github.ebaptistella.wire.in.str.str0008
  "Wire-in STR0008: raw XML → domain model using fields extracted by logic.str.parser."
  (:require [clojure.string :as str]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defn- extract-sender-ispb
  [queue-name]
  (try
    (nth (str/split queue-name #"\.") 2)
    (catch Exception _
      nil)))

(defmethod parse-inbound "STR0008"
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-fields body)]
    {:id             (str (UUID/randomUUID))
     :type           "STR0008"
     :num-ctrl-if    (:num-ctrl-if fields)
     :ispb-if-debtd  (:ispb-if-debtd fields)
     :ispb-if-credtd (:ispb-if-credtd fields)
     :vlr-lanc       (:vlr-lanc fields)
     :finldd-cli     (:finldd-cli fields)
     :dt-movto       (:dt-movto fields)
     :participant    (extract-sender-ispb queue-name)
     :queue-name     queue-name
     :message-id     message-id
     :body           body
     :received-at    (str (Instant/now))
     :status         :pending
     :direction      :inbound
     :response       nil}))
