(ns com.github.ebaptistella.wire.in.slb.slb0002r1
  "SLB0002R1: Response to SLB0002 debit request (account verification)."
  (:require [schema.core :as s]))

(s/defschema SLB0002R1Response
  {:NumCtrlPart                    (s/constrained s/Str #(not (empty? %)))
   :ISPBPart                       (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   :DtVenc                         (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   :VlrLanc                        (s/constrained s/Num #(and (>= % 0) (<= % 999999999.99)))
   (s/optional-key :SitDebito)     s/Str
   (s/optional-key :DtSitDebito)   (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :MotRecusa)     s/Str
   (s/optional-key :Hist)          s/Str})
