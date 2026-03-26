(ns com.github.ebaptistella.logic.str.str0014
  "STR0014: consulta de extrato → STR0014R1 com filtro por período."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(defn- movimento->xml [msg]
  (str "<Lancamento>"
       "<NumCtrlSTRLanc>" (xml/escape (get-in msg [:response :num-ctrl-str])) "</NumCtrlSTRLanc>"
       "<NumCtrlIFLanc>" (xml/escape (:num-ctrl-if msg)) "</NumCtrlIFLanc>"
       "<VlrLanc>" (or (:vlr-lanc msg) "0.00") "</VlrLanc>"
       "<SitLancSTR>" (parser/status->sit-lanc-str (:status msg)) "</SitLancSTR>"
       "<DtHrSit>" (or (get-in msg [:response :sent-at]) (:received-at msg)) "</DtHrSit>"
       "<ISPBIFDebtd>" (xml/escape (or (:ispb-if-debtd msg) "")) "</ISPBIFDebtd>"
       "<ISPBIFCredtd>" (xml/escape (or (:ispb-if-credtd msg) "")) "</ISPBIFCredtd>"
       "<TpLanc>" (parser/type->tp-lanc (or (:type msg) "")) "</TpLanc>"
       "</Lancamento>"))

(s/defn filter-extrato :- [s/Any]
  "Additional in-memory filter on store messages (already pre-filtered by get-by-period).
   Applies no additional filtering currently — provided for future extensibility."
  [store-msgs :- [s/Any]
   _parsed-msg :- {s/Keyword s/Any}]
  store-msgs)

(s/defn r1-response :- s/Str
  "Generates STR0014R1 XML from parsed inbound message and filtered movimentos."
  [parsed-msg :- {s/Keyword s/Any}
   movimentos :- [s/Any]]
  (let [now          (Instant/now)
        num-ctrl-str (xml/new-control-number)
        dt-hr-bc     (xml/format-datetime now)
        dt-movto     (or (:dt-ref parsed-msg) (xml/format-date now))
        qtd-lanc     (count movimentos)]
    (str "<STR0014R1>"
         "<CodMsg>STR0014R1</CodMsg>"
         "<NumCtrlIF>" (xml/escape (:num-ctrl-if parsed-msg)) "</NumCtrlIF>"
         "<ISPBIFDebtd>" (xml/escape (:ispb-if-debtd parsed-msg)) "</ISPBIFDebtd>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<DtMovto>" dt-movto "</DtMovto>"
         "<QtdLanc>" qtd-lanc "</QtdLanc>"
         (apply str (map movimento->xml movimentos))
         "</STR0014R1>")))
