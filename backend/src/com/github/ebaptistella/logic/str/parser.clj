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
  "Derives the R1 outbound queue from an inbound queue name.
   Convention: QL.{TYPE}.{SENDER}.{RECIPIENT}.NN → QR.{TYPE}.{RECIPIENT}.{SENDER}.NN"
  [queue-name :- s/Str]
  (let [parts (str/split queue-name #"\.")
        v     (vec parts)]
    (str/join "." (-> v (assoc 0 "QR") (assoc 2 (v 3)) (assoc 3 (v 2))))))

(s/defn r2-outbound-queue :- (s/maybe s/Str)
  "Derives the R2 outbound queue targeting ISPBIFCredtd.
   Builds from r1 queue and replaces the reader slot (index 3) with ispb-if-credtd."
  [queue-name :- s/Str
   ispb-if-credtd :- (s/maybe s/Str)]
  (when ispb-if-credtd
    (let [r1    (r1-outbound-queue queue-name)
          parts (str/split r1 #"\.")]
      (str/join "." (assoc (vec parts) 3 ispb-if-credtd)))))

(s/defn sender-ispb-from-queue :- (s/maybe s/Str)
  "Extracts the sender ISPB from a SPB queue name.
   Convention: QL.{TYPE}.{SENDER_ISPB}.{RECIPIENT_ISPB}.NN → SENDER_ISPB (index 2)."
  [queue-name :- s/Str]
  (try
    (nth (str/split queue-name #"\.") 2)
    (catch Exception _
      nil)))

(s/defn parse-str0001 :- {s/Keyword (s/maybe s/Str)}
  "Extracts STR0001 (horários STR query) relevant fields from XML body."
  [body :- s/Str]
  {:num-ctrl-if   (xml-value body "NumCtrlIF")
   :ispb-if-debtd (xml-value body "ISPBIFDebtd")
   :dt-ref        (xml-value body "DtRef")})

(s/defn parse-str0012 :- {s/Keyword (s/maybe s/Str)}
  "Extracts STR0012 (lançamentos query) relevant fields from XML body."
  [body :- s/Str]
  {:num-ctrl-if     (xml-value body "NumCtrlIF")
   :ispb-if-debtd   (xml-value body "ISPBIFDebtd")
   :dt-movto        (xml-value body "DtMovto")
   :num-ctrl-str-or (xml-value body "NumCtrlSTROr")
   :sit-lanc-str    (xml-value body "SitLancSTR")})

(s/defn parse-str0013 :- {s/Keyword (s/maybe s/Str)}
  "Extracts STR0013 (saldo query) relevant fields from XML body."
  [body :- s/Str]
  {:num-ctrl-if   (xml-value body "NumCtrlIF")
   :ispb-if-debtd (xml-value body "ISPBIFDebtd")
   :dt-ref        (xml-value body "DtRef")})

(s/defn parse-str0014 :- {s/Keyword (s/maybe s/Str)}
  "Extracts STR0014 (extrato query) relevant fields from XML body."
  [body :- s/Str]
  {:num-ctrl-if   (xml-value body "NumCtrlIF")
   :ispb-if-debtd (xml-value body "ISPBIFDebtd")
   :dt-ref        (xml-value body "DtRef")
   :hr-ini        (xml-value body "HrIni")
   :hr-fim        (xml-value body "HrFim")})

(s/defn status->sit-lanc-str :- s/Str
  "Maps store status keywords to STR SitLancSTR codes."
  [status :- s/Keyword]
  (case status
    :pending        "PENDENTE"
    :responded      "LQDADO"
    :auto-responded "LQDADO"
    "PENDENTE"))

(s/defn type->tp-lanc :- s/Str
  "Maps STR message type to TpLanc code for extrato responses."
  [msg-type :- s/Str]
  (case msg-type
    "STR0008" "TED"
    "OTR"))
