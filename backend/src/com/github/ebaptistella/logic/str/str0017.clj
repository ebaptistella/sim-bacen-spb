(ns com.github.ebaptistella.logic.str.str0017
  "STR0017: Aviso de Abertura do STR (Fluxo7 — broadcast)."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(s/defn build-message :- s/Str
  "Builds STR0017 XML broadcast (aviso de abertura).
   params may contain :hr-abertura and :hr-fechamento to override config values."
  [params :- {s/Keyword s/Any}
   config :- {s/Keyword s/Any}]
  (let [now           (Instant/now)
        num-ctrl-str  (xml/new-control-number)
        dt-hr-bc      (xml/format-datetime now)
        dt-movto      (xml/format-date now)
        hr-abertura   (xml/escape (or (:hr-abertura params)
                                      (:str-horario-abertura config)
                                      "07:00"))
        hr-fechamento (xml/escape (or (:hr-fechamento params)
                                      (:str-horario-fechamento config)
                                      "17:30"))]
    (str "<STR0017>"
         "<CodMsg>STR0017</CodMsg>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<DtMovto>" dt-movto "</DtMovto>"
         "<HrAbertura>" hr-abertura "</HrAbertura>"
         "<HrFechamento>" hr-fechamento "</HrFechamento>"
         "</STR0017>")))

(s/defn queue-name :- s/Str
  "Derives the outbound MQ queue for STR0017.
   Format: QR.REQ.{simulator-ispb}.{participant-ispb}.01"
  [simulator-ispb :- s/Str
   participant-ispb :- s/Str]
  (str "QR.REQ." simulator-ispb "." participant-ispb ".01"))
