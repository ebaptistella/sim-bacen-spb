(ns com.github.ebaptistella.wire.in.str.str0025
  "Wire-in STR0025: raw XML → domain model using parse-str0025 (TED para depósito judicial, extracts FinlddCli + TpCtDebtd + TpCtCredtd + Agencia + CtPgto + Hist)."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]])
  (:import [java.time Instant]
           [java.util UUID]))

(defmethod parse-inbound :STR0025
  [{:keys [queue-name message-id body]}]
  (let [fields (parser/parse-str0025 body)]
    {:id             (str (UUID/randomUUID))
     :type           :STR0025
     :num-ctrl-if    (:num-ctrl-if fields)
     :ispb-if-debtd  (:ispb-if-debtd fields)
     :ispb-if-credtd (:ispb-if-credtd fields)
     :vlr-lanc       (:vlr-lanc fields)
     :finldd-cli     (:finldd-cli fields)
     :tp-ct-debtd    (:tp-ct-debtd fields)
     :tp-ct-credtd   (:tp-ct-credtd fields)
     :agencia        (:agencia fields)
     :ct-pgto        (:ct-pgto fields)
     :hist           (:hist fields)
     :dt-movto       (:dt-movto fields)
     :participant    (parser/sender-ispb-from-queue queue-name)
     :queue-name     queue-name
     :message-id     message-id
     :body           body
     :received-at    (str (Instant/now))
     :status         :pending
     :direction      :inbound}))
