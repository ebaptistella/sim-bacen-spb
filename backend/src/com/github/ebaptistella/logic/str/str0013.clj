(ns com.github.ebaptistella.logic.str.str0013
  "STR0013: consulta de saldo → STR0013R1."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(s/defn r1-response :- s/Str
  "Generates STR0013R1 XML from parsed inbound message and simulated balance."
  [parsed-msg :- {s/Keyword s/Any}
   config :- {s/Keyword s/Any}]
  (let [now          (Instant/now)
        num-ctrl-str (xml/new-control-number)
        dt-hr-bc     (xml/format-datetime now)
        dt-movto     (or (:dt-ref parsed-msg) (xml/format-date now))
        sld-cnt-rsv  (xml/escape (or (:str-saldo-simulado config) "99999999.99"))]
    (str "<STR0013R1>"
         "<CodMsg>STR0013R1</CodMsg>"
         "<NumCtrlIF>" (xml/escape (:num-ctrl-if parsed-msg)) "</NumCtrlIF>"
         "<ISPBIFDebtd>" (xml/escape (:ispb-if-debtd parsed-msg)) "</ISPBIFDebtd>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<DtMovto>" dt-movto "</DtMovto>"
         "<SldCntRsv>" sld-cnt-rsv "</SldCntRsv>"
         "</STR0013R1>")))
