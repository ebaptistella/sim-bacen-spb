(ns com.github.ebaptistella.wire.in.slb.slb0007r1
  "SLB0007R1: Response to SLB0007 specific debit request."
  (:require [schema.core :as s]))

(s/defschema SLB0007R1Response
  {:NumCtrlPart                    (s/constrained s/Str #(not (empty? %)))
   :ISPBPart                       (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   :VlrLanc                        (s/constrained s/Num #(and (>= % 0) (<= % 999999999.99)))
   (s/optional-key :SitDebito)     s/Str
   (s/optional-key :DtSitDebito)   (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :MotRecusa)     s/Str
   (s/optional-key :Hist)          s/Str})
