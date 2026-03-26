(ns com.github.ebaptistella.logic.str.str0001
  "STR0001: query de horários do STR → STR0001R1 auto-response."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(s/defn r1-response :- s/Str
  "Generates STR0001R1 XML from parsed inbound message and config horários."
  [parsed-msg :- {s/Keyword s/Any}
   config :- {s/Keyword s/Any}]
  (let [now           (Instant/now)
        num-ctrl-str  (xml/new-control-number)
        dt-hr-bc      (xml/format-datetime now)
        dt-movto      (or (:dt-ref parsed-msg) (xml/format-date now))
        hr-abertura   (xml/escape (or (:str-horario-abertura config) "07:00"))
        hr-fechamento (xml/escape (or (:str-horario-fechamento config) "17:30"))]
    (str "<STR0001R1>"
         "<CodMsg>STR0001R1</CodMsg>"
         "<NumCtrlIF>" (xml/escape (:num-ctrl-if parsed-msg)) "</NumCtrlIF>"
         "<ISPBIFDebtd>" (xml/escape (:ispb-if-debtd parsed-msg)) "</ISPBIFDebtd>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<DtMovto>" dt-movto "</DtMovto>"
         "<HrAbertura>" hr-abertura "</HrAbertura>"
         "<HrFechamento>" hr-fechamento "</HrFechamento>"
         "</STR0001R1>")))
