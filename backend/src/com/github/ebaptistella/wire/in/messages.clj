(ns com.github.ebaptistella.wire.in.messages
  "Inbound HTTP request schemas for message endpoints."
  (:require [schema.core :as s]))

(s/defschema RespondBody
  {:response-type s/Str
   (s/optional-key :params) {s/Keyword s/Any}})

(s/defschema OutboundBody
  {:type                    (s/constrained s/Str #{"STR0015" "STR0016" "STR0017"})
   :participant             (s/constrained s/Str #(re-matches #"[0-9]{8}" %))
   (s/optional-key :params) {s/Any s/Any}})
