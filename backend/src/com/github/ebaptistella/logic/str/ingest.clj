(ns com.github.ebaptistella.logic.str.ingest
  "XML building for inbound message injection."
  (:require [clojure.data.xml :as xml]
            [schema.core :as s]))

(defn- escape-xml [s]
  (if (string? s)
    (-> s
        (clojure.string/replace "&" "&amp;")
        (clojure.string/replace "<" "&lt;")
        (clojure.string/replace ">" "&gt;")
        (clojure.string/replace "\"" "&quot;")
        (clojure.string/replace "'" "&apos;"))
    s))

(defn- build-element [tag value]
  (xml/element tag {} (escape-xml (str value))))

(defn- build-xml [msg-type fields]
  (let [tag-name (keyword msg-type)
        elements (concat
                   [(build-element :CodMsg msg-type)]
                   (mapcat (fn [[k v]]
                             (when v
                               [(build-element (keyword k) v)]))
                           fields))]
    (xml/indent-str
      (apply xml/element tag-name {} elements))))

(s/defn build-str0001-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0001" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:DtRef (:DtRef params)]
                        [:HrIni (:HrIni params)]
                        [:HrFim (:HrFim params)]]))

(s/defn build-str0003-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0003" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0004-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0004" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0005-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0005" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddCli (:FinlddCli params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0006-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0006" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddCli (:FinlddCli params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0007-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0007" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddCli (:FinlddCli params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0008-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0008" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddCli (:FinlddCli params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0010-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0010" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:NumCtrlSTROr (:NumCtrlSTROr params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:CodDevTransf (:CodDevTransf params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0011-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0011" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:NumCtrlSTROr (:NumCtrlSTROr params)]
                        [:DtMovto (:DtMovto params)]]))

(s/defn build-str0012-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0012" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:DtMovto (:DtMovto params)]
                        [:HrIni (:HrIni params)]
                        [:HrFim (:HrFim params)]]))

(s/defn build-str0013-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0013" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:DtMovto (:DtMovto params)]
                        [:HrIni (:HrIni params)]
                        [:HrFim (:HrFim params)]]))

(s/defn build-str0014-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0014" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:DtMovto (:DtMovto params)]
                        [:HrIni (:HrIni params)]
                        [:HrFim (:HrFim params)]]))

(s/defn build-str0020-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0020" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0021-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0021" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0022-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0022" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0025-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0025" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0026-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0026" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0029-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0029" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0033-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0033" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0034-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0034" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0035-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0035" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:DtRef (:DtRef params)]
                        [:HrIni (:HrIni params)]
                        [:HrFim (:HrFim params)]]))

(s/defn build-str0037-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0037" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0039-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0039" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0040-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0040" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0041-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0041" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0043-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0043" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:DtMovto (:DtMovto params)]
                        [:Hist (:Hist params)]]))

(s/defn build-str0044-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0044" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:DtMovto (:DtMovto params)]
                        [:Hist (:Hist params)]]))

(s/defn build-str0045-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0045" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0046-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0046" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0047-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0047" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0048-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0048" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:NumCtrlSTROr (:NumCtrlSTROr params)]
                        [:CodDevTransf (:CodDevTransf params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0051-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0051" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

(s/defn build-str0052-xml [params :- {s/Keyword s/Any}]
  (build-xml "STR0052" [[:NumCtrlIF (:NumCtrlIF params)]
                        [:ISPBIFDebtd (:ISPBIFDebtd params)]
                        [:ISPBIFCredtd (:ISPBIFCredtd params)]
                        [:VlrLanc (:VlrLanc params)]
                        [:FinlddIF (:FinlddIF params)]
                        [:DtMovto (:DtMovto params)]
                        [:TpCtDebtd (:TpCtDebtd params)]
                        [:TpCtCredtd (:TpCtCredtd params)]]))

;; Builder map for lookup
(def builders
  {"STR0001" build-str0001-xml
   "STR0003" build-str0003-xml
   "STR0004" build-str0004-xml
   "STR0005" build-str0005-xml
   "STR0006" build-str0006-xml
   "STR0007" build-str0007-xml
   "STR0008" build-str0008-xml
   "STR0010" build-str0010-xml
   "STR0011" build-str0011-xml
   "STR0012" build-str0012-xml
   "STR0013" build-str0013-xml
   "STR0014" build-str0014-xml
   "STR0020" build-str0020-xml
   "STR0021" build-str0021-xml
   "STR0022" build-str0022-xml
   "STR0025" build-str0025-xml
   "STR0026" build-str0026-xml
   "STR0029" build-str0029-xml
   "STR0033" build-str0033-xml
   "STR0034" build-str0034-xml
   "STR0035" build-str0035-xml
   "STR0037" build-str0037-xml
   "STR0039" build-str0039-xml
   "STR0040" build-str0040-xml
   "STR0041" build-str0041-xml
   "STR0043" build-str0043-xml
   "STR0044" build-str0044-xml
   "STR0045" build-str0045-xml
   "STR0046" build-str0046-xml
   "STR0047" build-str0047-xml
   "STR0048" build-str0048-xml
   "STR0051" build-str0051-xml
   "STR0052" build-str0052-xml})

(s/defn build-xml-for-type [msg-type :- s/Str, params :- {s/Keyword s/Any}]
  (if-let [builder (get builders msg-type)]
    (builder params)
    (throw (ex-info "Unknown message type" {:type msg-type}))))
