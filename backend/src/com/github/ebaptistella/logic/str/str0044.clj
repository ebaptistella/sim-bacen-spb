(ns com.github.ebaptistella.logic.str.str0044
  "Pure STR0044 logic: cancelamento de teste de contingência Internet. Fluxo1: apenas R1."
  (:require [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(def ^:private field-ordering
  {:STR0044R1 [:CodMsg :NumCtrlIF :ISPBIFDebtd :NumCtrlSTR :SitLancSTR :DtHrSit]})

(s/def InboundMessage
  {:num-ctrl-if   (s/maybe s/Str)
   :ispb-if-debtd (s/maybe s/Str)
   :dt-movto      (s/maybe s/Str)
   s/Keyword      s/Any})

(s/def OverrideParams {s/Keyword s/Any})

(s/defn r1-response :- {s/Keyword s/Any}
  [msg :- InboundMessage
   params :- (s/maybe OverrideParams)]
  (let [p   (or params {})
        now (Instant/now)
        sit (or (:SitLancSTR p) (:sit-lanc-str p) "LIQUIDADO")]
    {:CodMsg      :STR0044R1
     :NumCtrlIF   (:num-ctrl-if msg)
     :ISPBIFDebtd (:ispb-if-debtd msg)
     :NumCtrlSTR  (xml/new-control-number)
     :SitLancSTR  sit
     :DtHrSit     (xml/format-datetime now)}))

(s/defn response->xml :- (s/maybe s/Str)
  [response-type :- s/Keyword
   fields-map :- {s/Keyword s/Any}]
  (when-let [ordered (get field-ordering response-type)]
    (let [tag   (name response-type)
          parts (for [k ordered
                      :let [v (get fields-map k)]
                      :when (some? v)]
                  (str "<" (name k) ">" (xml/escape (if (keyword? v) (name v) v)) "</" (name k) ">"))]
      (str "<" tag ">" (apply str parts) "</" tag ">"))))
