(ns com.github.ebaptistella.wire.in.slb.slb0003
  "SLB0003: Unidirectional debit operation."
  (:require [schema.core :as s]))

(s/defschema SLB0003Request
  {:ISPBPart                      (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :NumCtrlSLBOr) s/Str
   (s/optional-key :DtMovto)      (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :VlrLanc)      (s/constrained s/Num #(and (>= % 0) (<= % 999999999.99)))
   (s/optional-key :Hist)         s/Str
   (s/optional-key :FIndddSLB)    s/Str})
