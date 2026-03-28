(ns com.github.ebaptistella.wire.in.str.str0041
  "Wire-in STR0041: raw XML → domain model using parse-str0041 (transferência para consignação em IF de aposentado, extracts FinlddCli + TpCtDebtd + TpCtCredtd + Agencia)."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defmethod parse-inbound :STR0041
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-str0041 body)]
    {:id             (str (UUID/randomUUID))
     :type           :STR0041
     :num-ctrl-if    (:num-ctrl-if fields)
     :ispb-if-debtd  (:ispb-if-debtd fields)
     :ispb-if-credtd (:ispb-if-credtd fields)
     :vlr-lanc       (:vlr-lanc fields)
     :finldd-cli     (:finldd-cli fields)
     :tp-ct-debtd    (:tp-ct-debtd fields)
     :tp-ct-credtd   (:tp-ct-credtd fields)
     :agencia        (:agencia fields)
     :dt-movto       (:dt-movto fields)
     :participant    (parser/sender-ispb-from-queue queue-name)
     :queue-name     queue-name
     :message-id     message-id
     :body           body
     :received-at    (str (Instant/now))
     :status         :pending
     :direction      :inbound}))
