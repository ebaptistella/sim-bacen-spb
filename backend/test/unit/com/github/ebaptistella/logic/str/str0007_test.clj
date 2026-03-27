(ns com.github.ebaptistella.logic.str.str0007-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0007 :as str0007]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-007"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "4000.00"
   :finldd-if      "00001"
   :dt-movto       "20260103"})

(deftest r1-response-test
  (testing "CodMsg is STR0007R1"
    (let [fields (str0007/r1-response base-msg nil)]
      (is (= "STR0007R1" (:CodMsg fields)))))
  (testing "SitLancSTR defaults to LQDADO"
    (let [fields (str0007/r1-response base-msg nil)]
      (is (= "LQDADO" (:SitLancSTR fields)))))
  (testing "NumCtrlIF is echoed from msg"
    (let [fields (str0007/r1-response base-msg nil)]
      (is (= "NC-007" (:NumCtrlIF fields)))))
  (testing "ISPBIFDebtd is echoed from msg"
    (let [fields (str0007/r1-response base-msg nil)]
      (is (= "00000000" (:ISPBIFDebtd fields)))))
  (testing "NumCtrlSTR is present with 20 alphanumeric chars"
    (let [fields   (str0007/r1-response base-msg nil)
          num-ctrl (:NumCtrlSTR fields)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl)))))
  (testing "DtHrSit is present and not blank"
    (let [fields (str0007/r1-response base-msg nil)]
      (is (some? (:DtHrSit fields)))
      (is (not (str/blank? (:DtHrSit fields))))))
  (testing "DtMovto echoes :dt-movto from msg"
    (let [fields (str0007/r1-response base-msg nil)]
      (is (= "20260103" (:DtMovto fields)))))
  (testing "SitLancSTR overridden via params"
    (let [fields (str0007/r1-response base-msg {:SitLancSTR "REJEITADO"})]
      (is (= "REJEITADO" (:SitLancSTR fields))))))

(deftest r2-response-usa-finldd-if-test
  (testing "CodMsg is STR0007R2"
    (let [fields (str0007/r2-response base-msg nil)]
      (is (= "STR0007R2" (:CodMsg fields)))))
  (testing "ISPBIFDebtd is echoed from msg"
    (let [fields (str0007/r2-response base-msg nil)]
      (is (= "00000000" (:ISPBIFDebtd fields)))))
  (testing "ISPBIFCredtd is echoed from msg"
    (let [fields (str0007/r2-response base-msg nil)]
      (is (= "11111111" (:ISPBIFCredtd fields)))))
  (testing "VlrLanc is echoed from msg"
    (let [fields (str0007/r2-response base-msg nil)]
      (is (= "4000.00" (:VlrLanc fields)))))
  (testing "FinlddIF is echoed from msg (not FinlddCli)"
    (let [fields (str0007/r2-response base-msg nil)]
      (is (= "00001" (:FinlddIF fields)))
      (is (nil? (:FinlddCli fields)))))
  (testing "XML contains FinlddIF and does not contain FinlddCli"
    (let [fields (str0007/r2-response base-msg nil)
          xml    (str0007/response->xml :STR0007R2 fields)]
      (is (str/includes? xml "<FinlddIF>00001</FinlddIF>"))
      (is (not (str/includes? xml "<FinlddCli>")))))
  (testing "NumCtrlSTR is present with 20 alphanumeric chars"
    (let [fields   (str0007/r2-response base-msg nil)
          num-ctrl (:NumCtrlSTR fields)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl)))))
  (testing "NumCtrlSTR from params is preserved"
    (let [fields (str0007/r2-response base-msg {:NumCtrlSTR "abc123fixed00000000x"})]
      (is (= "abc123fixed00000000x" (:NumCtrlSTR fields)))))
  (testing "DtHrBC is present and not blank"
    (let [fields (str0007/r2-response base-msg nil)]
      (is (some? (:DtHrBC fields)))
      (is (not (str/blank? (:DtHrBC fields)))))))

(deftest rejection-response-sem-motivo-test
  (testing "nil params returns :missing-motivo error"
    (let [result (str0007/rejection-response base-msg nil)]
      (is (= {:error :missing-motivo} result))))
  (testing "empty params map returns :missing-motivo error"
    (let [result (str0007/rejection-response base-msg {})]
      (is (= {:error :missing-motivo} result))))
  (testing "params with blank MotivoRejeicao returns :missing-motivo error"
    (let [result (str0007/rejection-response base-msg {:MotivoRejeicao ""})]
      (is (= {:error :missing-motivo} result)))))

(deftest rejection-response-com-motivo-test
  (testing "CodMsg is STR0007E"
    (let [fields (str0007/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= "STR0007E" (:CodMsg fields)))))
  (testing "NumCtrlIF is echoed from msg"
    (let [fields (str0007/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= "NC-007" (:NumCtrlIF fields)))))
  (testing "ISPBIFDebtd is echoed from msg"
    (let [fields (str0007/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= "00000000" (:ISPBIFDebtd fields)))))
  (testing "valid MotivoRejeicao AC09 appears in XML"
    (let [fields (str0007/rejection-response base-msg {:MotivoRejeicao "AC09"})
          xml    (str0007/response->xml :STR0007E fields)]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>")))))

(deftest response-xml-test
  (testing "response->xml STR0007R1 starts and ends with correct tags"
    (let [fields (str0007/r1-response base-msg nil)
          xml    (str0007/response->xml :STR0007R1 fields)]
      (is (str/starts-with? xml "<STR0007R1>"))
      (is (str/ends-with? xml "</STR0007R1>"))))
  (testing "response->xml STR0007R1 contains CodMsg STR0007R1"
    (let [fields (str0007/r1-response base-msg nil)
          xml    (str0007/response->xml :STR0007R1 fields)]
      (is (str/includes? xml "<CodMsg>STR0007R1</CodMsg>"))))
  (testing "response->xml STR0007R2 starts and ends with correct tags"
    (let [fields (str0007/r2-response base-msg nil)
          xml    (str0007/response->xml :STR0007R2 fields)]
      (is (str/starts-with? xml "<STR0007R2>"))
      (is (str/ends-with? xml "</STR0007R2>"))))
  (testing "response->xml STR0007E contains CodMsg STR0007E"
    (let [fields (str0007/rejection-response base-msg {:MotivoRejeicao "AC09"})
          xml    (str0007/response->xml :STR0007E fields)]
      (is (str/includes? xml "<CodMsg>STR0007E</CodMsg>")))))
