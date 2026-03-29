(ns com.github.ebaptistella.wire.in.slb.slb0001
  "SLB0001: Unidirectional debit notification from BACEN."
  (:require [schema.core :as s]))

(s/defschema SLB0001Request
  {:ISPBPart                      (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :NumCtrlSLB)    s/Str
   (s/optional-key :DtVenc)        (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :VlrLanc)       (s/constrained s/Num #(and (>= % 0) (<= % 999999999.99)))
   (s/optional-key :FIndddSLB)     s/Str
   (s/optional-key :Hist)          s/Str
   (s/optional-key :DtMovto)       (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :NumCtrlSLBOr)  s/Str
   (s/optional-key :CNPJConv)      s/Str
   (s/optional-key :AgDebdt)       s/Str
   (s/optional-key :TpCtDebdt)     s/Str
   (s/optional-key :CtDebdt)       s/Str
   (s/optional-key :TpPessoaDebdt) s/Str
   (s/optional-key :CNPJCPFDebdt)  s/Str})
