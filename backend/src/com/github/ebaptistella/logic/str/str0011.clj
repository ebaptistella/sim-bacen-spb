(ns com.github.ebaptistella.logic.str.str0011
  "Pure STR0011 logic: field maps and XML serialization for R1 (cancelado) and E (rejeitado) responses."
  (:require [clojure.string :as str]
            [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(def ^:private field-ordering
  {:STR0011R1 [:CodMsg :NumCtrlIF :ISPBIFDebtd :NumCtrlSTR :SitLancSTR :DtHrSit]
   :STR0011E  [:CodMsg :NumCtrlIF :ISPBIFDebtd :MotivoRejeicao]})

(s/def InboundMessage
  {:num-ctrl-if   (s/maybe s/Str)
   :ispb-if-debtd (s/maybe s/Str)
   s/Keyword      s/Any})

(s/def OverrideParams {s/Keyword s/Any})

(s/defn r1-response :- {s/Keyword s/Any}
  [msg :- InboundMessage
   params :- (s/maybe OverrideParams)]
  (let [now (Instant/now)]
    {:CodMsg      :STR0011R1
     :NumCtrlIF   (:num-ctrl-if msg)
     :ISPBIFDebtd (:ispb-if-debtd msg)
     :NumCtrlSTR  (xml/new-control-number)
     :SitLancSTR  "CANCELADO"
     :DtHrSit     (xml/format-datetime now)}))

(s/defn rejection-response :- (s/conditional
                                 #(contains? % :error) {:error (s/eq :missing-motivo)}
                                 :else {s/Keyword s/Any})
  [msg :- InboundMessage
   params :- (s/maybe OverrideParams)]
  (let [p      (or params {})
        motivo (or (:MotivoRejeicao p) (:motivo-rejeicao p))]
    (if (or (nil? motivo) (str/blank? (str motivo)))
      {:error :missing-motivo}
      {:CodMsg         :STR0011E
       :NumCtrlIF      (:num-ctrl-if msg)
       :ISPBIFDebtd    (:ispb-if-debtd msg)
       :MotivoRejeicao (str motivo)})))

(s/defn response->xml :- s/Str
  [response-type :- s/Keyword
   fields-map :- {s/Keyword s/Any}]
  (let [ordered (get field-ordering response-type)
        tag     (name response-type)
        parts   (for [k ordered
                      :let [v (get fields-map k)]
                      :when (some? v)]
                  (str "<" (name k) ">" (xml/escape (if (keyword? v) (name v) v)) "</" (name k) ">"))]
    (str "<" tag ">" (apply str parts) "</" tag ">")))
