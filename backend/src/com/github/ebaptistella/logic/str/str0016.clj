(ns com.github.ebaptistella.logic.str.str0016
  "STR0016: Saldo no fechamento por participante (Fluxo5 — unicast)."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(s/defn build-message :- s/Str
  "Builds STR0016 XML (saldo no fechamento).
   params may contain :saldo and :hr-fechamento to override config values."
  [params :- {s/Keyword s/Any}
   participant :- s/Str
   config :- {s/Keyword s/Any}]
  (let [now           (Instant/now)
        num-ctrl-str  (xml/new-control-number)
        dt-hr-bc      (xml/format-datetime now)
        dt-movto      (xml/format-date now)
        sld-cnt-rsv   (xml/escape (or (:saldo params)
                                      (:str-saldo-simulado config)
                                      "99999999.99"))
        hr-fechamento (xml/escape (or (:hr-fechamento params)
                                      (:str-horario-fechamento config)
                                      "17:30"))]
    (str "<STR0016>"
         "<CodMsg>STR0016</CodMsg>"
         "<NumCtrlSTR>" num-ctrl-str "</NumCtrlSTR>"
         "<DtHrBC>" dt-hr-bc "</DtHrBC>"
         "<DtMovto>" dt-movto "</DtMovto>"
         "<ISPBIFDebtd>" (xml/escape participant) "</ISPBIFDebtd>"
         "<SldCntRsv>" sld-cnt-rsv "</SldCntRsv>"
         "<HrFechamento>" hr-fechamento "</HrFechamento>"
         "</STR0016>")))

(s/defn queue-name :- s/Str
  "Derives the outbound MQ queue for STR0016.
   Format: QR.REQ.{simulator-ispb}.{participant-ispb}.01"
  [simulator-ispb :- s/Str
   participant-ispb :- s/Str]
  (str "QR.REQ." simulator-ispb "." participant-ispb ".01"))
