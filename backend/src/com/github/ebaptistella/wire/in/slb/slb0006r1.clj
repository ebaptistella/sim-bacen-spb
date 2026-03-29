(ns com.github.ebaptistella.wire.in.slb.slb0006r1
  "SLB0006R1: Response to SLB0006 position query (account position)."
  (:require [schema.core :as s]))

(s/defschema SLB0006R1Response
  {:NumCtrlPart                    (s/constrained s/Str #(not (empty? %)))
   :ISPBPart                       (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   :VlrDebito                      (s/constrained s/Num #(>= % 0))
   :VlrCredito                     (s/constrained s/Num #(>= % 0))
   (s/optional-key :Saldo)         (s/constrained s/Num #(>= % 0))
   (s/optional-key :DtSaldo)       (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :Hist)          s/Str})
