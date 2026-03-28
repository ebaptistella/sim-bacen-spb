(ns com.github.ebaptistella.wire.in.str.str0043
  "Wire-in STR0043: raw XML → domain model using parse-str0043 (agendamento de teste de contingência Internet)."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defmethod parse-inbound :STR0043
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-str0043 body)]
    {:id            (str (UUID/randomUUID))
     :type          :STR0043
     :num-ctrl-if   (:num-ctrl-if fields)
     :ispb-if-debtd (:ispb-if-debtd fields)
     :dt-movto      (:dt-movto fields)
     :participant   (parser/sender-ispb-from-queue queue-name)
     :queue-name    queue-name
     :message-id    message-id
     :body          body
     :received-at   (str (Instant/now))
     :status        :pending
     :direction     :inbound}))
