(ns com.github.ebaptistella.logic.str.str0042
  "STR0042: Aviso de início/fim de otimização (Fluxo7 — broadcast)."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(s/defn build-message :- s/Str
  "Builds STR0042 XML broadcast (aviso de início/fim de otimização).
   params may contain :ind-otimizacao (\"I\"=início, \"F\"=fim)."
  [params :- {s/Keyword s/Any}
   config :- {s/Keyword s/Any}]
  (let [now           (Instant/now)
        num-ctrl-str  (xml/new-control-number)
        dt-hr-bc      (xml/format-datetime now)
        dt-movto      (xml/format-date now)
        ind-otimizacao (xml/escape (or (:ind-otimizacao params) "I"))]
    (str "<STR0042>"
         "<CodMsg>STR0042</CodMsg>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<DtMovto>" dt-movto "</DtMovto>"
         "<IndOtimizacao>" ind-otimizacao "</IndOtimizacao>"
         "</STR0042>")))

(s/defn queue-name :- s/Str
  "Derives the outbound MQ queue for STR0042.
   Format: QR.REQ.{simulator-ispb}.{participant-ispb}.01"
  [simulator-ispb :- s/Str
   participant-ispb :- s/Str]
  (str "QR.REQ." simulator-ispb "." participant-ispb ".01"))
