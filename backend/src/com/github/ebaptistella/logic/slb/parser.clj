(ns com.github.ebaptistella.logic.slb.parser
  "XML parsing for SLB response messages."
  (:require [schema.core :as s]
            [clojure.string :as str]
            [clojure.xml :as xml])
  (:import [java.io StringReader]))

(defn- unescape-xml [s]
  (if (string? s)
    (-> s
        (str/replace "&apos;" "'")
        (str/replace "&quot;" "\"")
        (str/replace "&gt;" ">")
        (str/replace "&lt;" "<")
        (str/replace "&amp;" "&"))
    s))

(defn- text-content [element]
  (when-let [content (:content element)]
    (str/join "" (filter string? content))))

(defn- find-tag [element tag-name]
  (->> (:content element)
       (filter #(and (map? %) (= (:tag %) tag-name)))
       first
       text-content
       unescape-xml))

(defn- parse-repeating-group [element group-tag-pattern]
  "Parse repeating groups like Grupo_SLB0006R1_Lanc repeated multiple times."
  (->> (:content element)
       (filter #(and (map? %) (str/includes? (str (:tag %)) group-tag-pattern)))
       (mapv (fn [group]
               (into {} (for [child (:content group)]
                          (when (map? child)
                            [(:tag child) (text-content child)])))))))

(s/defn parse-slb-response :- {s/Keyword s/Any}
  "Parse SLB response XML and extract all fields."
  [xml-string :- s/Str]
  (try
    (let [element (xml/parse (StringReader. xml-string))
          tag-name (:tag element)
          msg-type (name tag-name)
          base {:type msg-type}
          common-fields {:CodMsg (find-tag element :CodMsg)
                         :NumCtrlPart (find-tag element :NumCtrlPart)
                         :NumCtrlSLB (find-tag element :NumCtrlSLB)
                         :ISPBPart (find-tag element :ISPBPart)
                         :VlrLanc (find-tag element :VlrLanc)
                         :DtVenc (find-tag element :DtVenc)
                         :DtMovto (find-tag element :DtMovto)
                         :Hist (find-tag element :Hist)}]
      (if (str/includes? msg-type "SLB0006R1")
        (let [grupo (parse-repeating-group element "Grupo_SLB0006R1")]
          (merge base common-fields {:data grupo}))
        (merge base (into {} (filter (fn [[_ v]] (not (nil? v))) common-fields)))))
    (catch Exception e
      (throw (ex-info "Failed to parse SLB XML" {:error (.getMessage e) :xml xml-string})))))

(s/defn get-msg-type-from-xml :- s/Str
  "Extract message type from XML tag name."
  [xml-string :- s/Str]
  (try
    (let [element (xml/parse (StringReader. xml-string))]
      (name (:tag element)))
    (catch Exception _
      nil)))
