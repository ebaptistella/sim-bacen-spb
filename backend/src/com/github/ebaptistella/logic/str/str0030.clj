(ns com.github.ebaptistella.logic.str.str0030
  "STR0030: Aptidão para abertura (Fluxo5/7 — broadcast)."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(s/defn build-message :- s/Str
  "Builds STR0030 XML broadcast (aptidão para abertura)."
  [params :- {s/Keyword s/Any}
   config :- {s/Keyword s/Any}]
  (let [now          (Instant/now)
        num-ctrl-str (xml/new-control-number)
        dt-hr-bc     (xml/format-datetime now)
        dt-movto     (xml/format-date now)]
    (str "<STR0030>"
         "<CodMsg>STR0030</CodMsg>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<DtMovto>" dt-movto "</DtMovto>"
         "</STR0030>")))

(s/defn queue-name :- s/Str
  "Derives the outbound MQ queue for STR0030.
   Format: QR.REQ.{simulator-ispb}.{participant-ispb}.01"
  [simulator-ispb :- s/Str
   participant-ispb :- s/Str]
  (str "QR.REQ." simulator-ispb "." participant-ispb ".01"))
