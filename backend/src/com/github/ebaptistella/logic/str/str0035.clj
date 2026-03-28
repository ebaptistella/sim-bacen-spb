(ns com.github.ebaptistella.logic.str.str0035
  "STR0035: consulta de extrato de tarifação → STR0035R1 com lista simulada vazia."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(s/defn r1-response :- s/Str
  "Generates STR0035R1 XML from parsed inbound message.
   Returns a simulated empty tariff statement (QtdTarif=0, no Tarif entries)."
  [parsed-msg :- {s/Keyword s/Any}]
  (let [now          (Instant/now)
        num-ctrl-str (xml/new-control-number)
        dt-hr-bc     (xml/format-datetime now)]
    (str "<STR0035R1>"
         "<CodMsg>STR0035R1</CodMsg>"
         "<NumCtrlIF>" (xml/escape (:num-ctrl-if parsed-msg)) "</NumCtrlIF>"
         "<ISPBIFDebtd>" (xml/escape (:ispb-if-debtd parsed-msg)) "</ISPBIFDebtd>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<QtdTarif>0</QtdTarif>"
         "</STR0035R1>")))
