(ns com.github.ebaptistella.schema
  "Utility functions for creating schemas from skeleton definitions."
  (:require [schema.core :as s]))

(s/defn strict-schema
  "Creates a strict schema from skeleton definition.
   Only allows keys defined in skeleton."
  [skeleton]
  (reduce-kv
   (fn [acc k v]
     (let [schema-type (:schema v)
           required? (:required v)]
       (if required?
         (assoc acc k schema-type)
         (assoc acc (s/optional-key k) schema-type))))
   {}
   skeleton))

(s/defn loose-schema
  "Creates a loose schema from skeleton definition.
   Allows extra keys not defined in skeleton."
  [skeleton]
  (strict-schema skeleton))
