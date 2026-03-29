(ns com.github.ebaptistella.wire.in.slb.slb0006
  "SLB0006: Consultation request with optional filters."
  (:require [schema.core :as s]))

(s/defschema SLB0006Request
  {:NumCtrlPart        s/Str
   :ISPBPart           (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :DtRef) (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :TpDeb_Cred) s/Str
   (s/optional-key :NumCtrlSLB) s/Str})
