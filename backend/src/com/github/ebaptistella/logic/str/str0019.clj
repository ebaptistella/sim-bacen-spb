(ns com.github.ebaptistella.logic.str.str0019
  "STR0019: Aviso de Inclusão/Alteração de participante (Fluxo7 — broadcast)."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(s/defn build-message :- s/Str
  "Builds STR0019 XML broadcast (aviso de inclusão/alteração de participante).
   params may contain :ispb-participante to identify the affected participant."
  [params :- {s/Keyword s/Any}
   config :- {s/Keyword s/Any}]
  (let [now          (Instant/now)
        num-ctrl-str (xml/new-control-number)
        dt-hr-bc     (xml/format-datetime now)
        dt-movto     (xml/format-date now)
        ispb-part    (xml/escape (or (:ispb-participante params)
                                     (:simulator-ispb config)
                                     "00000000"))]
    (str "<STR0019>"
         "<CodMsg>STR0019</CodMsg>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<DtMovto>" dt-movto "</DtMovto>"
         "<ISPBParticipante>" ispb-part "</ISPBParticipante>"
         "</STR0019>")))

(s/defn queue-name :- s/Str
  "Derives the outbound MQ queue for STR0019.
   Format: QR.REQ.{simulator-ispb}.{participant-ispb}.01"
  [simulator-ispb :- s/Str
   participant-ispb :- s/Str]
  (str "QR.REQ." simulator-ispb "." participant-ispb ".01"))
