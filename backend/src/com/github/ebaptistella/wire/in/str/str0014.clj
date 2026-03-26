(ns com.github.ebaptistella.wire.in.str.str0014
  "Wire-in STR0014: raw XML → domain model for extrato query."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defmethod parse-inbound "STR0014"
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-str0014 body)]
    {:id            (str (UUID/randomUUID))
     :type          "STR0014"
     :num-ctrl-if   (:num-ctrl-if fields)
     :ispb-if-debtd (:ispb-if-debtd fields)
     :dt-ref        (:dt-ref fields)
     :hr-ini        (:hr-ini fields)
     :hr-fim        (:hr-fim fields)
     :participant   (parser/sender-ispb-from-queue queue-name)
     :queue-name    queue-name
     :message-id    message-id
     :body          body
     :received-at   (str (Instant/now))
     :status        :auto-responded
     :direction     :inbound
     :response      nil}))
