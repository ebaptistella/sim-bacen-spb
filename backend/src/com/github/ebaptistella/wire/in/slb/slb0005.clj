(ns com.github.ebaptistella.wire.in.slb.slb0005
  "SLB0005: Credit notification from BACEN."
  (:require [schema.core :as s]))

(s/defschema SLB0005Request
  {:NumCtrlSTR         s/Str
   :ISPBPart           (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   :VlrLanc            (s/constrained s/Num #(and (>= % 0) (<= % 999999999.99)))
   :FIndddSLB          s/Str
   :NumCtrlSLB         s/Str
   :DtVenc             (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :Hist) s/Str
   (s/optional-key :NumCtrlSLBOr) s/Str
   (s/optional-key :CNPJConv) s/Str
   (s/optional-key :CodIBANCredtdo) s/Str
   (s/optional-key :AgCredtd) s/Str
   (s/optional-key :TpCtCredtd) s/Str
   (s/optional-key :CtCredtd) s/Str
   (s/optional-key :TpPessoaCredtd) s/Str
   (s/optional-key :CNPJCPFCredtd) s/Str
   (s/optional-key :NomClieCredtd) s/Str})
