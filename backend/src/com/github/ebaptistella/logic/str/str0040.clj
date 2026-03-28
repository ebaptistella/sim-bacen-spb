(ns com.github.ebaptistella.logic.str.str0040
  "Pure STR0040 logic: field maps and XML serialization for R1, R2 and E responses."
  (:require [clojure.string :as str]
            [com.github.ebaptistella.logic.str.xml :as xml]
            [schema.core :as s])
  (:import [java.time Instant]))

(def ^:private field-ordering
  {:STR0040R1 [:CodMsg :NumCtrlIF :ISPBIFDebtd :NumCtrlSTR :SitLancSTR :DtHrSit :DtMovto]
   :STR0040R2 [:CodMsg :ISPBIFDebtd :ISPBIFCredtd :VlrLanc :FinlddIF :NumCtrlSTR :DtHrBC]
   :STR0040E  [:CodMsg :NumCtrlIF :ISPBIFDebtd :MotivoRejeicao]})

(s/def InboundMessage
  {:num-ctrl-if    (s/maybe s/Str)
   :ispb-if-debtd  (s/maybe s/Str)
   :ispb-if-credtd (s/maybe s/Str)
   :vlr-lanc       (s/maybe s/Str)
   :finldd-if      (s/maybe s/Str)
   :dt-movto       (s/maybe s/Str)
   s/Keyword       s/Any})

(s/def OverrideParams {s/Keyword s/Any})

(s/defn r1-response :- {s/Keyword s/Any}
  [msg :- InboundMessage
   params :- (s/maybe OverrideParams)]
  (let [p   (or params {})
        now (Instant/now)
        sit (or (:SitLancSTR p) (:sit-lanc-str p) "LQDADO")]
    {:CodMsg      :STR0040R1
     :NumCtrlIF   (:num-ctrl-if msg)
     :ISPBIFDebtd (:ispb-if-debtd msg)
     :NumCtrlSTR  (xml/new-control-number)
     :SitLancSTR  sit
     :DtHrSit     (xml/format-datetime now)
     :DtMovto     (or (:dt-movto msg) (xml/format-date now))}))

(s/defn r2-response :- {s/Keyword s/Any}
  [msg :- InboundMessage
   params :- (s/maybe OverrideParams)]
  (let [now (Instant/now)
        p   (or params {})]
    {:CodMsg       :STR0040R2
     :ISPBIFDebtd  (:ispb-if-debtd msg)
     :ISPBIFCredtd (:ispb-if-credtd msg)
     :VlrLanc      (:vlr-lanc msg)
     :FinlddIF     (:finldd-if msg)
     :NumCtrlSTR   (or (:NumCtrlSTR p) (xml/new-control-number))
     :DtHrBC       (xml/format-datetime now)}))

(s/defn rejection-response :- (s/conditional
                                 #(contains? % :error) {:error (s/eq :missing-motivo)}
                                 :else {s/Keyword s/Any})
  [msg :- InboundMessage
   params :- (s/maybe OverrideParams)]
  (let [p      (or params {})
        motivo (or (:MotivoRejeicao p) (:motivo-rejeicao p))]
    (if (or (nil? motivo) (str/blank? (str motivo)))
      {:error :missing-motivo}
      {:CodMsg         :STR0040E
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
