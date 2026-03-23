(ns com.github.ebaptistella.logic.str.str0008
  "Pure STR0008 logic: field maps and XML serialization for R1, R2 and E responses."
  (:require [clojure.string :as str]
            [schema.core :as s])
  (:import [java.time Instant ZoneId]
           [java.time.format DateTimeFormatter]
           [java.util UUID]))

(def ^:private zone-br (ZoneId/of "America/Sao_Paulo"))

(def ^:private fmt-datetime (DateTimeFormatter/ofPattern "yyyyMMddHHmmss"))

(def ^:private fmt-date (DateTimeFormatter/ofPattern "yyyyMMdd"))

(defn- new-control-number []
  (-> (str (UUID/randomUUID))
      (str/replace "-" "")
      (subs 0 20)))

(defn- format-datetime [^Instant inst]
  (.format fmt-datetime (.atZone inst zone-br)))

(defn- format-date [^Instant inst]
  (.format fmt-date (.atZone inst zone-br)))

(defn- escape-xml [v]
  (when (some? v)
    (-> (str v)
        (str/replace "&" "&amp;")
        (str/replace "<" "&lt;")
        (str/replace ">" "&gt;")
        (str/replace "\"" "&quot;"))))

(def ^:private field-ordering
  {"STR0008R1" [:CodMsg :NumCtrlIF :ISPBIFDebtd :NumCtrlSTR :SitLancSTR :DtHrSit :DtMovto]
   "STR0008R2" [:CodMsg :ISPBIFDebtd :ISPBIFCredtd :VlrLanc :FinlddCli :NumCtrlSTR :DtHrBC]
   "STR0008E"  [:CodMsg :NumCtrlIF :ISPBIFDebtd :MotivoRejeicao]})

(s/def InboundMessage
  {:num-ctrl-if    (s/maybe s/Str)
   :ispb-if-debtd  (s/maybe s/Str)
   :ispb-if-credtd (s/maybe s/Str)
   :vlr-lanc       (s/maybe s/Str)
   :finldd-cli     (s/maybe s/Str)
   :dt-movto       (s/maybe s/Str)
   s/Keyword       s/Any})

(s/def OverrideParams {s/Keyword s/Any})

(s/defn r1-response :- {s/Keyword s/Any}
  [msg :- InboundMessage
   params :- (s/maybe OverrideParams)]
  (let [p   (or params {})
        now (Instant/now)
        sit (or (:SitLancSTR p) (:sit-lanc-str p) "LQDADO")]
    {:CodMsg      "STR0008R1"
     :NumCtrlIF   (:num-ctrl-if msg)
     :ISPBIFDebtd (:ispb-if-debtd msg)
     :NumCtrlSTR  (new-control-number)
     :SitLancSTR  sit
     :DtHrSit     (format-datetime now)
     :DtMovto     (format-date now)}))

(s/defn r2-response :- {s/Keyword s/Any}
  [msg :- InboundMessage
   _params :- (s/maybe OverrideParams)]
  (let [now (Instant/now)]
    {:CodMsg       "STR0008R2"
     :ISPBIFDebtd  (:ispb-if-debtd msg)
     :ISPBIFCredtd (:ispb-if-credtd msg)
     :VlrLanc      (:vlr-lanc msg)
     :FinlddCli    (:finldd-cli msg)
     :NumCtrlSTR   (new-control-number)
     :DtHrBC       (format-datetime now)}))

(s/defn rejection-response :- (s/conditional
                                 #(contains? % :error) {:error (s/eq :missing-motivo)}
                                 :else {s/Keyword s/Any})
  [msg :- InboundMessage
   params :- (s/maybe OverrideParams)]
  (let [p      (or params {})
        motivo (or (:MotivoRejeicao p) (:motivo-rejeicao p))]
    (if (or (nil? motivo) (str/blank? (str motivo)))
      {:error :missing-motivo}
      {:CodMsg         "STR0008E"
       :NumCtrlIF      (:num-ctrl-if msg)
       :ISPBIFDebtd    (:ispb-if-debtd msg)
       :MotivoRejeicao (str motivo)})))

(s/defn response->xml :- s/Str
  [response-type :- s/Str
   fields-map :- {s/Keyword s/Any}]
  (let [ordered (get field-ordering response-type)
        parts   (for [k ordered
                      :let [v (get fields-map k)]
                      :when (some? v)]
                  (str "<" (name k) ">" (escape-xml v) "</" (name k) ">"))]
    (str "<" response-type ">" (apply str parts) "</" response-type ">")))
