(ns com.github.ebaptistella.wire.in.str.str0001
  "Wire-in STR0001: raw XML → domain model for STR schedule query."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defmethod parse-inbound :STR0001
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-str0001 body)]
    {:id            (str (UUID/randomUUID))
     :type          :STR0001
     :num-ctrl-if   (:num-ctrl-if fields)
     :ispb-if-debtd (:ispb-if-debtd fields)
     :dt-ref        (:dt-ref fields)
     :participant   (parser/sender-ispb-from-queue queue-name)
     :queue-name    queue-name
     :message-id    message-id
     :body          body
     :received-at   (str (Instant/now))
     :status        :auto-responded
     :direction     :inbound
     :response      nil}))
