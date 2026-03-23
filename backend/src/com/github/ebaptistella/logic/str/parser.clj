(ns com.github.ebaptistella.logic.str.parser
  "STR XML field extraction via regex — aligned with wire.in.str."
  (:require [clojure.string :as str]
            [schema.core :as s]))

(def ^:private header-tags
  ["CodMsg" "NumCtrlIF" "NumCtrlSTR" "NumCtrlSTROr"])

(def ^:private transfer-tags
  ["ISPBIFDebtd" "ISPBIFCredtd" "VlrLanc" "FinlddCli" "DtMovto"])

(defn- xml-value [body tag]
  (when (and body (not (str/blank? body)))
    (let [v (second (re-find (re-pattern (str "<" tag ">([^<]*)</" tag ">")) body))]
      (when-not (str/blank? v) (str/trim v)))))

(defn- xml-tag->key [tag]
  (case tag
    "CodMsg" :cod-msg
    "NumCtrlIF" :num-ctrl-if
    "NumCtrlSTR" :num-ctrl-str
    "NumCtrlSTROr" :num-ctrl-str-or
    "ISPBIFDebtd" :ispb-if-debtd
    "ISPBIFCredtd" :ispb-if-credtd
    "VlrLanc" :vlr-lanc
    "FinlddCli" :finldd-cli
    "DtMovto" :dt-movto))

(def ^:private blank-fields
  (into {}
        (map (fn [tag] [(xml-tag->key tag) nil]))
        (concat header-tags transfer-tags)))

(s/defn parse-fields :- {s/Keyword (s/maybe s/Str)}
  [body]
  (if (or (nil? body) (str/blank? body))
    blank-fields
    (into {}
          (map (fn [tag] [(xml-tag->key tag) (xml-value body tag)]))
          (concat header-tags transfer-tags))))

(s/defn r1-outbound-queue :- s/Str
  [queue-name :- s/Str]
  (str/replace queue-name #"^QL" "QR"))

(s/defn r2-outbound-queue :- (s/maybe s/Str)
  [queue-name :- s/Str
   ispb-if-credtd :- (s/maybe s/Str)]
  (when ispb-if-credtd
    (let [r1    (r1-outbound-queue queue-name)
          parts (str/split r1 #"\.")]
      (str/join "." (assoc (vec parts) 2 ispb-if-credtd)))))
