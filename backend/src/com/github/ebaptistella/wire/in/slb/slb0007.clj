(ns com.github.ebaptistella.wire.in.slb.slb0007
  "SLB0007: Request-response debit operation 2."
  (:require [schema.core :as s]))

(s/defschema SLB0007Request
  {:NumCtrlPart        s/Str
   :ISPBPart           (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   :VlrLanc            (s/constrained s/Num #(and (>= % 0) (<= % 999999999.99)))
   (s/optional-key :DtMovto) (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :Hist) s/Str
   (s/optional-key :DtVenc) (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :FIndddSLB) s/Str})
