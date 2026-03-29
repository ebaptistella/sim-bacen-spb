(ns com.github.ebaptistella.wire.in.slb.slb0008
  "SLB0008: Unidirectional debit operation 3."
  (:require [schema.core :as s]))

(s/defschema SLB0008Request
  {:NumCtrlSLB         s/Str
   :ISPBPart           (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   :VlrLanc            (s/constrained s/Num #(and (>= % 0) (<= % 999999999.99)))
   (s/optional-key :Hist) s/Str
   (s/optional-key :DtVenc) (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :DtMovto) (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :FIndddSLB) s/Str})
