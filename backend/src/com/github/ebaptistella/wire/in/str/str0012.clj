(ns com.github.ebaptistella.wire.in.str.str0012
  "Wire-in STR0012: raw XML → domain model for lançamentos query."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defmethod parse-inbound :STR0012
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-str0012 body)]
    {:id               (str (UUID/randomUUID))
     :type             :STR0012
     :num-ctrl-if      (:num-ctrl-if fields)
     :ispb-if-debtd    (:ispb-if-debtd fields)
     :dt-movto         (:dt-movto fields)
     :num-ctrl-str-or  (:num-ctrl-str-or fields)
     :sit-lanc-str     (:sit-lanc-str fields)
     :participant      (parser/sender-ispb-from-queue queue-name)
     :queue-name       queue-name
     :message-id       message-id
     :body             body
     :received-at      (str (Instant/now))
     :status           :auto-responded
     :direction        :inbound}))
