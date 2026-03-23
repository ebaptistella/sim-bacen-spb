(ns com.github.ebaptistella.wire.in.messages
  "Inbound HTTP request schemas for message endpoints."
  (:require [schema.core :as s]))

(s/defschema RespondBody
  {:response-type (s/enum "STR0008R1" "STR0008R2" "STR0008E")
   (s/optional-key :params) {s/Keyword s/Any}})
