(ns com.github.ebaptistella.wire.in.str.str0004
  "Wire-in STR0004: raw XML → domain model using parse-str0004 (transferência entre IFs por conta própria)."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defmethod parse-inbound :STR0004
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-str0004 body)]
    {:id             (str (UUID/randomUUID))
     :type           :STR0004
     :num-ctrl-if    (:num-ctrl-if fields)
     :ispb-if-debtd  (:ispb-if-debtd fields)
     :ispb-if-credtd (:ispb-if-credtd fields)
     :vlr-lanc       (:vlr-lanc fields)
     :finldd-if      (:finldd-if fields)
     :dt-movto       (:dt-movto fields)
     :participant    (parser/sender-ispb-from-queue queue-name)
     :queue-name     queue-name
     :message-id     message-id
     :body           body
     :received-at    (str (Instant/now))
     :status         :pending
     :direction      :inbound}))
