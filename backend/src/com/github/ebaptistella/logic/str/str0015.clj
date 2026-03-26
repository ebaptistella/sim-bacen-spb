(ns com.github.ebaptistella.logic.str.str0015
  "STR0015: Aviso de Fechamento do STR (Fluxo7 — broadcast)."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(s/defn build-message :- s/Str
  "Builds STR0015 XML broadcast (aviso de fechamento).
   params may contain :hr-fechamento to override config value."
  [params :- {s/Keyword s/Any}
   config :- {s/Keyword s/Any}]
  (let [now           (Instant/now)
        num-ctrl-str  (xml/new-control-number)
        dt-hr-bc      (xml/format-datetime now)
        dt-movto      (xml/format-date now)
        hr-fechamento (xml/escape (or (:hr-fechamento params)
                                      (:str-horario-fechamento config)
                                      "17:30"))]
    (str "<STR0015>"
         "<CodMsg>STR0015</CodMsg>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<DtMovto>" dt-movto "</DtMovto>"
         "<HrFechamento>" hr-fechamento "</HrFechamento>"
         "</STR0015>")))

(s/defn queue-name :- s/Str
  "Derives the outbound MQ queue for STR0015.
   Format: QR.REQ.{simulator-ispb}.{participant-ispb}.01"
  [simulator-ispb :- s/Str
   participant-ispb :- s/Str]
  (str "QR.REQ." simulator-ispb "." participant-ispb ".01"))
