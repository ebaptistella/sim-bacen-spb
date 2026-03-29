(ns com.github.ebaptistella.wire.in.slb.slb0001
  "SLB0001: Unidirectional debit notification from BACEN."
  (:require [schema.core :as s]))

(s/defschema SLB0001Request
  {:NumCtrlSLB         s/Str
   :ISPBPart           (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   :DtVenc             (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   :VlrLanc            (s/constrained s/Num #(and (>= % 0) (<= % 999999999.99)))
   :FIndddSLB          s/Str
   (s/optional-key :Hist) s/Str
   (s/optional-key :DtMovto) (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :NumCtrlSLBOr) s/Str
   (s/optional-key :CNPJConv) s/Str
   (s/optional-key :AgDebdt) s/Str
   (s/optional-key :TpCtDebdt) s/Str
   (s/optional-key :CtDebdt) s/Str
   (s/optional-key :TpPessoaDebdt) s/Str
   (s/optional-key :CNPJCPFDebdt) s/Str})
