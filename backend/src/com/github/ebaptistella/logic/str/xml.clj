(ns com.github.ebaptistella.logic.str.xml
  "Shared XML utilities for STR message builders: escaping, formatting, control numbers."
  (:require [clojure.string :as str])
  (:import [java.time Instant ZoneId]
           [java.time.format DateTimeFormatter]
           [java.util UUID]))

(def ^:private zone-br (ZoneId/of "America/Sao_Paulo"))
(def ^:private fmt-datetime (DateTimeFormatter/ofPattern "yyyyMMddHHmmss"))
(def ^:private fmt-date (DateTimeFormatter/ofPattern "yyyyMMdd"))

(defn new-control-number
  "Generates a random 20-character alphanumeric control number."
  []
  (-> (str (UUID/randomUUID))
      (str/replace "-" "")
      (subs 0 20)))

(defn format-datetime
  "Formats an Instant as 'yyyyMMddHHmmss' in Brazil/São Paulo timezone."
  [^Instant inst]
  (.format fmt-datetime (.atZone inst zone-br)))

(defn format-date
  "Formats an Instant as 'yyyyMMdd' in Brazil/São Paulo timezone."
  [^Instant inst]
  (.format fmt-date (.atZone inst zone-br)))

(defn escape
  "Escapes XML special characters in v. Returns nil when v is nil."
  [v]
  (when (some? v)
    (-> (str v)
        (str/replace "&" "&amp;")
        (str/replace "<" "&lt;")
        (str/replace ">" "&gt;")
        (str/replace "\"" "&quot;"))))
