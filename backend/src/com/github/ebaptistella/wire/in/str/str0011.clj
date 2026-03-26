(ns com.github.ebaptistella.wire.in.str.str0011
  "Wire-in STR0011: raw XML → domain model."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defmethod parse-inbound "STR0011"
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-fields body)]
    {:id              (str (UUID/randomUUID))
     :type            "STR0011"
     :num-ctrl-if     (:num-ctrl-if fields)
     :ispb-if-debtd   (:ispb-if-debtd fields)
     :num-ctrl-str-or (:num-ctrl-str-or fields)
     :participant     (parser/sender-ispb-from-queue queue-name)
     :queue-name      queue-name
     :message-id      message-id
     :body            body
     :received-at     (str (Instant/now))
     :status          :pending
     :direction       :inbound
     :response        nil}))
