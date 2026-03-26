(ns com.github.ebaptistella.logic.str.str0012
  "STR0012: consulta de lançamentos → STR0012R1 com filtro do store."
  (:require [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(defn- lancamento->xml [msg]
  (str "<Lancamento>"
       "<NumCtrlSTRLanc>" (xml/escape (get-in msg [:response :num-ctrl-str])) "</NumCtrlSTRLanc>"
       "<NumCtrlIFLanc>" (xml/escape (:num-ctrl-if msg)) "</NumCtrlIFLanc>"
       "<SitLancSTR>" (parser/status->sit-lanc-str (:status msg)) "</SitLancSTR>"
       "<VlrLanc>" (or (:vlr-lanc msg) "0.00") "</VlrLanc>"
       "<DtHrSit>" (or (get-in msg [:response :sent-at]) (:received-at msg)) "</DtHrSit>"
       "<ISPBIFDebtd>" (xml/escape (or (:ispb-if-debtd msg) "")) "</ISPBIFDebtd>"
       "<ISPBIFCredtd>" (xml/escape (or (:ispb-if-credtd msg) "")) "</ISPBIFCredtd>"
       "</Lancamento>"))

(s/defn filter-lancamentos :- [s/Any]
  "Filters store messages by NumCtrlSTROr (optional) and SitLancSTR (optional).
   store-msgs: seq of message maps from store, pre-filtered by DtMovto.
   parsed-msg: domain model from wire-in STR0012."
  [store-msgs :- [s/Any]
   parsed-msg :- {s/Keyword s/Any}]
  (let [num-ctrl-str-or (:num-ctrl-str-or parsed-msg)
        sit-filter      (:sit-lanc-str parsed-msg)]
    (cond->> store-msgs
      num-ctrl-str-or
      (filter #(= (get-in % [:response :num-ctrl-str]) num-ctrl-str-or))

      sit-filter
      (filter #(= (parser/status->sit-lanc-str (:status %)) sit-filter)))))

(s/defn r1-response :- s/Str
  "Generates STR0012R1 XML from parsed inbound message and filtered lancamentos list."
  [parsed-msg :- {s/Keyword s/Any}
   lancamentos :- [s/Any]]
  (let [now          (Instant/now)
        num-ctrl-str (xml/new-control-number)
        dt-hr-bc     (xml/format-datetime now)
        qtd-lanc     (count lancamentos)]
    (str "<STR0012R1>"
         "<CodMsg>STR0012R1</CodMsg>"
         "<NumCtrlIF>" (xml/escape (:num-ctrl-if parsed-msg)) "</NumCtrlIF>"
         "<ISPBIFDebtd>" (xml/escape (:ispb-if-debtd parsed-msg)) "</ISPBIFDebtd>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<QtdLanc>" qtd-lanc "</QtdLanc>"
         (apply str (map lancamento->xml lancamentos))
         "</STR0012R1>")))
