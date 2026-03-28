(ns com.github.ebaptistella.wire.in.str.str0044
  "Wire-in STR0044: raw XML → domain model using parse-str0044 (cancelamento de teste de contingência Internet)."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defmethod parse-inbound :STR0044
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-str0044 body)]
    {:id            (str (UUID/randomUUID))
     :type          :STR0044
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
