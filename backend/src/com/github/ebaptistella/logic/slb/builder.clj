(ns com.github.ebaptistella.logic.slb.builder
  "XML construction for SLB messages (builders for each type)."
  (:require [schema.core :as s]
            [clojure.string :as str]
            [com.github.ebaptistella.logic.slb.correlation :as correlation])
  (:import [java.time Instant]))

(defn- escape-xml [s]
  (if (string? s)
    (-> s
        (str/replace "&" "&amp;")
        (str/replace "<" "&lt;")
        (str/replace ">" "&gt;")
        (str/replace "\"" "&quot;")
        (str/replace "'" "&apos;"))
    s))

(defn- xml-field [tag value]
  (when value
    (str "<" tag ">" (escape-xml (str value)) "</" tag ">")))

(s/defn build-slb0001-xml :- s/Str
  "Build SLB0001 XML: unidirectional debit notification."
  [data :- {s/Keyword s/Any}]
  (str
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    "<SLB0001>"
    (xml-field "CodMsg" "SLB0001")
    (xml-field "NumCtrlSLB" (:NumCtrlSLB data))
    (xml-field "ISPBPart" (:ISPBPart data))
    (xml-field "DtVenc" (:DtVenc data))
    (xml-field "VlrLanc" (:VlrLanc data))
    (xml-field "FIndddSLB" (:FIndddSLB data))
    (xml-field "Hist" (:Hist data))
    (xml-field "DtMovto" (:DtMovto data))
    (xml-field "NumCtrlSLBOr" (:NumCtrlSLBOr data))
    (xml-field "CNPJConv" (:CNPJConv data))
    (xml-field "AgDebdt" (:AgDebdt data))
    (xml-field "TpCtDebdt" (:TpCtDebdt data))
    (xml-field "CtDebdt" (:CtDebdt data))
    "</SLB0001>"))

(s/defn build-slb0002-xml :- s/Str
  "Build SLB0002 XML: request-response debit operation."
  [data :- {s/Keyword s/Any}]
  (str
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    "<SLB0002>"
    (xml-field "CodMsg" "SLB0002")
    (xml-field "NumCtrlPart" (:NumCtrlPart data))
    (xml-field "ISPBPart" (:ISPBPart data))
    (xml-field "VlrLanc" (:VlrLanc data))
    (xml-field "DtMovto" (:DtMovto data))
    (xml-field "Hist" (:Hist data))
    (xml-field "DtVenc" (:DtVenc data))
    (xml-field "FIndddSLB" (:FIndddSLB data))
    "</SLB0002>"))

(s/defn build-slb0003-xml :- s/Str
  "Build SLB0003 XML: unidirectional debit operation."
  [data :- {s/Keyword s/Any}]
  (str
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    "<SLB0003>"
    (xml-field "CodMsg" "SLB0003")
    (xml-field "NumCtrlSLBOr" (:NumCtrlSLBOr data))
    (xml-field "ISPBPart" (:ISPBPart data))
    (xml-field "DtMovto" (:DtMovto data))
    (xml-field "VlrLanc" (:VlrLanc data))
    (xml-field "Hist" (:Hist data))
    (xml-field "FIndddSLB" (:FIndddSLB data))
    "</SLB0003>"))

(s/defn build-slb0005-xml :- s/Str
  "Build SLB0005 XML: credit notification."
  [data :- {s/Keyword s/Any}]
  (str
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    "<SLB0005>"
    (xml-field "CodMsg" "SLB0005")
    (xml-field "NumCtrlSTR" (:NumCtrlSTR data))
    (xml-field "ISPBPart" (:ISPBPart data))
    (xml-field "VlrLanc" (:VlrLanc data))
    (xml-field "FIndddSLB" (:FIndddSLB data))
    (xml-field "NumCtrlSLB" (:NumCtrlSLB data))
    (xml-field "DtVenc" (:DtVenc data))
    (xml-field "Hist" (:Hist data))
    (xml-field "NumCtrlSLBOr" (:NumCtrlSLBOr data))
    (xml-field "CodIBANCredtdo" (:CodIBANCredtdo data))
    "</SLB0005>"))

(s/defn build-slb0006-xml :- s/Str
  "Build SLB0006 XML: consultation request with optional filters."
  [data :- {s/Keyword s/Any}]
  (str
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    "<SLB0006>"
    (xml-field "CodMsg" "SLB0006")
    (xml-field "NumCtrlPart" (:NumCtrlPart data))
    (xml-field "ISPBPart" (:ISPBPart data))
    (xml-field "DtRef" (:DtRef data))
    (xml-field "TpDeb_Cred" (:TpDeb_Cred data))
    (xml-field "NumCtrlSLB" (:NumCtrlSLB data))
    "</SLB0006>"))

(s/defn build-slb0007-xml :- s/Str
  "Build SLB0007 XML: request-response debit operation 2."
  [data :- {s/Keyword s/Any}]
  (str
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    "<SLB0007>"
    (xml-field "CodMsg" "SLB0007")
    (xml-field "NumCtrlPart" (:NumCtrlPart data))
    (xml-field "ISPBPart" (:ISPBPart data))
    (xml-field "VlrLanc" (:VlrLanc data))
    (xml-field "DtMovto" (:DtMovto data))
    (xml-field "Hist" (:Hist data))
    "</SLB0007>"))

(s/defn build-slb0008-xml :- s/Str
  "Build SLB0008 XML: unidirectional debit operation 3."
  [data :- {s/Keyword s/Any}]
  (str
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    "<SLB0008>"
    (xml-field "CodMsg" "SLB0008")
    (xml-field "NumCtrlSLB" (:NumCtrlSLB data))
    (xml-field "ISPBPart" (:ISPBPart data))
    (xml-field "VlrLanc" (:VlrLanc data))
    (xml-field "Hist" (:Hist data))
    (xml-field "DtVenc" (:DtVenc data))
    "</SLB0008>"))

(s/defn get-builder :- (s/pred fn?)
  "Get the builder function for a given SLB message type."
  [msg-type :- s/Str]
  (case msg-type
    "SLB0001" build-slb0001-xml
    "SLB0002" build-slb0002-xml
    "SLB0003" build-slb0003-xml
    "SLB0005" build-slb0005-xml
    "SLB0006" build-slb0006-xml
    "SLB0007" build-slb0007-xml
    "SLB0008" build-slb0008-xml
    nil))
