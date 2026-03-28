(ns com.github.ebaptistella.logic.str.str0050
  "STR0050: Requisição de início de encerramento do RDC (Fluxo10 — outbound)."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(s/defn build-message :- s/Str
  "Builds STR0050 XML message (requisição de início de encerramento do RDC)."
  [params :- {s/Keyword s/Any}
   config :- {s/Keyword s/Any}]
  (let [now          (Instant/now)
        num-ctrl-str (xml/new-control-number)
        dt-hr-bc     (xml/format-datetime now)
        dt-movto     (xml/format-date now)]
    (str "<STR0050>"
         "<CodMsg>STR0050</CodMsg>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<DtMovto>" dt-movto "</DtMovto>"
         "</STR0050>")))

(s/defn queue-name :- s/Str
  "Derives the outbound MQ queue for STR0050.
   Format: QR.REQ.{simulator-ispb}.{participant-ispb}.01"
  [simulator-ispb :- s/Str
   participant-ispb :- s/Str]
  (str "QR.REQ." simulator-ispb "." participant-ispb ".01"))
