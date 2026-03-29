(ns com.github.ebaptistella.wire.in.str.ingest
  "Inbound JSON schemas for HTTP message injection endpoints."
  (:require [schema.core :as s]))

;; Common field types
(def NumCtrlIF (s/constrained s/Str #(re-matches #".{1,30}" %)))
(def ISPBDebtd (s/constrained s/Str #(re-matches #"[0-9]{8}" %)))
(def ISPBCredtd (s/constrained s/Str #(re-matches #"[0-9]{8}" %)))
(def VlrLanc (s/constrained s/Str #(re-matches #"[0-9]{1,15}(\.[0-9]{1,2})?" %)))
(def DtMovto (s/constrained s/Str #(re-matches #"[0-9]{8}" %)))
(def DtRef (s/constrained s/Str #(re-matches #"[0-9]{8}" %)))
(def FinlddCli (s/constrained s/Str #(re-matches #"[0-9]{1,5}" %)))
(def FinlddIF (s/constrained s/Str #(re-matches #"[0-9]{1,5}" %)))
(def NumCtrlSTROr (s/constrained s/Str #(re-matches #".{1,30}" %)))
(def CodDevTransf (s/constrained s/Str #(re-matches #"[0-9]{2,3}" %)))
(def HrIni (s/constrained s/Str #(re-matches #"[0-9]{4,6}" %)))
(def HrFim (s/constrained s/Str #(re-matches #"[0-9]{4,6}" %)))

;; Fluxo2 messages (TED, Repasse, etc.) - R1+R2+E
(s/defschema STR0003IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0004IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0005IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddCli FinlddCli
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0006IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddCli FinlddCli
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0007IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddCli FinlddCli
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0008IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddCli FinlddCli
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

;; Fluxo2 Devolução
(s/defschema STR0010IngestSchema
  {:NumCtrlIF NumCtrlIF
   :NumCtrlSTROr NumCtrlSTROr
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :CodDevTransf CodDevTransf
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

;; Fluxo1 Cancelamento (R1 only)
(s/defschema STR0011IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :NumCtrlSTROr NumCtrlSTROr
   (s/optional-key :DtMovto) DtMovto})

;; Fluxo4 Queries (auto-respond)
(s/defschema STR0001IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :DtRef DtRef
   (s/optional-key :HrIni) HrIni
   (s/optional-key :HrFim) HrFim})

(s/defschema STR0012IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :DtMovto DtMovto
   (s/optional-key :HrIni) HrIni
   (s/optional-key :HrFim) HrFim})

(s/defschema STR0013IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :DtMovto DtMovto
   (s/optional-key :HrIni) HrIni
   (s/optional-key :HrFim) HrFim})

(s/defschema STR0014IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :DtMovto DtMovto
   (s/optional-key :HrIni) HrIni
   (s/optional-key :HrFim) HrFim})

(s/defschema STR0035IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :DtRef DtRef
   :HrIni HrIni
   :HrFim HrFim})

;; Repasses and other Fluxo2
(s/defschema STR0020IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0021IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0022IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0025IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0026IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0029IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0033IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0034IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0037IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0039IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0040IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0041IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

;; Fluxo1 Contingency
(s/defschema STR0043IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :DtMovto DtMovto
   (s/optional-key :Hist) s/Str})

(s/defschema STR0044IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :DtMovto DtMovto
   (s/optional-key :Hist) s/Str})

;; More Fluxo2
(s/defschema STR0045IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0046IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0047IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0048IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :NumCtrlSTROr) NumCtrlSTROr
   (s/optional-key :CodDevTransf) CodDevTransf
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0051IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

(s/defschema STR0052IngestSchema
  {:NumCtrlIF NumCtrlIF
   :ISPBIFDebtd ISPBDebtd
   :ISPBIFCredtd ISPBCredtd
   :VlrLanc VlrLanc
   :FinlddIF FinlddIF
   :DtMovto DtMovto
   (s/optional-key :TpCtDebtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str})

;; Schema map for lookup
(def schemas
  {"STR0001" STR0001IngestSchema
   "STR0003" STR0003IngestSchema
   "STR0004" STR0004IngestSchema
   "STR0005" STR0005IngestSchema
   "STR0006" STR0006IngestSchema
   "STR0007" STR0007IngestSchema
   "STR0008" STR0008IngestSchema
   "STR0010" STR0010IngestSchema
   "STR0011" STR0011IngestSchema
   "STR0012" STR0012IngestSchema
   "STR0013" STR0013IngestSchema
   "STR0014" STR0014IngestSchema
   "STR0020" STR0020IngestSchema
   "STR0021" STR0021IngestSchema
   "STR0022" STR0022IngestSchema
   "STR0025" STR0025IngestSchema
   "STR0026" STR0026IngestSchema
   "STR0029" STR0029IngestSchema
   "STR0033" STR0033IngestSchema
   "STR0034" STR0034IngestSchema
   "STR0035" STR0035IngestSchema
   "STR0037" STR0037IngestSchema
   "STR0039" STR0039IngestSchema
   "STR0040" STR0040IngestSchema
   "STR0041" STR0041IngestSchema
   "STR0043" STR0043IngestSchema
   "STR0044" STR0044IngestSchema
   "STR0045" STR0045IngestSchema
   "STR0046" STR0046IngestSchema
   "STR0047" STR0047IngestSchema
   "STR0048" STR0048IngestSchema
   "STR0051" STR0051IngestSchema
   "STR0052" STR0052IngestSchema})

(s/defn get-schema [msg-type :- s/Str]
  (get schemas msg-type))
