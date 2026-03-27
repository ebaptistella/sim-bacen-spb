(ns com.github.ebaptistella.wire.in.str.str0048
  "Wire-in STR0048: raw XML → domain model."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defmethod parse-inbound :STR0048
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-str0048 body)]
    {:id               (str (UUID/randomUUID))
     :type             :STR0048
     :num-ctrl-if      (:num-ctrl-if fields)
     :num-ctrl-str-or  (:num-ctrl-str-or fields)
     :ispb-if-debtd    (:ispb-if-debtd fields)
     :ispb-if-credtd   (:ispb-if-credtd fields)
     :vlr-lanc         (:vlr-lanc fields)
     :cod-dev-transf   (:cod-dev-transf fields)
     :dt-movto         (:dt-movto fields)
     :ispb-if-devedora (:ispb-if-devedora fields)
     :participant      (parser/sender-ispb-from-queue queue-name)
     :queue-name       queue-name
     :message-id       message-id
     :body             body
     :received-at      (str (Instant/now))
     :status           :pending
     :direction        :inbound}))
