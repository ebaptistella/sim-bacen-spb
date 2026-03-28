(ns com.github.ebaptistella.logic.str.str0025-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0025 :as str0025]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-025"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "5000.00"
   :finldd-cli     "0017"
   :dt-movto       "20260103"})

(deftest r1-response-test
  (testing "CodMsg is STR0025R1"
    (let [fields (str0025/r1-response base-msg nil)]
      (is (= :STR0025R1 (:CodMsg fields)))))
  (testing "SitLancSTR defaults to LQDADO"
    (let [fields (str0025/r1-response base-msg nil)]
      (is (= "LQDADO" (:SitLancSTR fields)))))
  (testing "NumCtrlIF is echoed from msg"
    (let [fields (str0025/r1-response base-msg nil)]
      (is (= "NC-025" (:NumCtrlIF fields)))))
  (testing "ISPBIFDebtd is echoed from msg"
    (let [fields (str0025/r1-response base-msg nil)]
      (is (= "00000000" (:ISPBIFDebtd fields)))))
  (testing "NumCtrlSTR is present with 20 alphanumeric chars"
    (let [fields   (str0025/r1-response base-msg nil)
          num-ctrl (:NumCtrlSTR fields)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl)))))
  (testing "DtHrSit is present and not blank"
    (let [fields (str0025/r1-response base-msg nil)]
      (is (some? (:DtHrSit fields)))
      (is (not (str/blank? (:DtHrSit fields))))))
  (testing "DtMovto echoes :dt-movto from msg"
    (let [fields (str0025/r1-response base-msg nil)]
      (is (= "20260103" (:DtMovto fields)))))
  (testing "SitLancSTR overridden via params"
    (let [fields (str0025/r1-response base-msg {:SitLancSTR "REJEITADO"})]
      (is (= "REJEITADO" (:SitLancSTR fields))))))

(deftest r2-response-finldd-cli-test
  (testing "CodMsg is STR0025R2"
    (let [fields (str0025/r2-response base-msg nil)]
      (is (= :STR0025R2 (:CodMsg fields)))))
  (testing "ISPBIFDebtd is echoed from msg"
    (let [fields (str0025/r2-response base-msg nil)]
      (is (= "00000000" (:ISPBIFDebtd fields)))))
  (testing "ISPBIFCredtd is echoed from msg"
    (let [fields (str0025/r2-response base-msg nil)]
      (is (= "11111111" (:ISPBIFCredtd fields)))))
  (testing "VlrLanc is echoed from msg"
    (let [fields (str0025/r2-response base-msg nil)]
      (is (= "5000.00" (:VlrLanc fields)))))
  (testing "FinlddCli is echoed from msg (not FinlddIF)"
    (let [fields (str0025/r2-response base-msg nil)]
      (is (= "0017" (:FinlddCli fields)))
      (is (nil? (:FinlddIF fields)))))
  (testing "XML contains FinlddCli and does not contain FinlddIF"
    (let [fields (str0025/r2-response base-msg nil)
          xml    (str0025/response->xml :STR0025R2 fields)]
      (is (str/includes? xml "<FinlddCli>0017</FinlddCli>"))
      (is (not (str/includes? xml "<FinlddIF>")))))
  (testing "NumCtrlSTR is present with 20 alphanumeric chars"
    (let [fields   (str0025/r2-response base-msg nil)
          num-ctrl (:NumCtrlSTR fields)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl)))))
  (testing "NumCtrlSTR from params is preserved"
    (let [fields (str0025/r2-response base-msg {:NumCtrlSTR "abc123fixed00000000x"})]
      (is (= "abc123fixed00000000x" (:NumCtrlSTR fields)))))
  (testing "DtHrBC is present and not blank"
    (let [fields (str0025/r2-response base-msg nil)]
      (is (some? (:DtHrBC fields)))
      (is (not (str/blank? (:DtHrBC fields)))))))

(deftest rejection-response-sem-motivo-test
  (testing "nil params returns :missing-motivo error"
    (let [result (str0025/rejection-response base-msg nil)]
      (is (= {:error :missing-motivo} result))))
  (testing "empty params map returns :missing-motivo error"
    (let [result (str0025/rejection-response base-msg {})]
      (is (= {:error :missing-motivo} result))))
  (testing "params with blank MotivoRejeicao returns :missing-motivo error"
    (let [result (str0025/rejection-response base-msg {:MotivoRejeicao ""})]
      (is (= {:error :missing-motivo} result)))))

(deftest rejection-response-com-motivo-test
  (testing "CodMsg is STR0025E"
    (let [fields (str0025/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= :STR0025E (:CodMsg fields)))))
  (testing "NumCtrlIF is echoed from msg"
    (let [fields (str0025/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= "NC-025" (:NumCtrlIF fields)))))
  (testing "ISPBIFDebtd is echoed from msg"
    (let [fields (str0025/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= "00000000" (:ISPBIFDebtd fields)))))
  (testing "valid MotivoRejeicao AC09 appears in XML"
    (let [fields (str0025/rejection-response base-msg {:MotivoRejeicao "AC09"})
          xml    (str0025/response->xml :STR0025E fields)]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>")))))

(deftest response-xml-test
  (testing "response->xml STR0025R1 starts and ends with correct tags"
    (let [fields (str0025/r1-response base-msg nil)
          xml    (str0025/response->xml :STR0025R1 fields)]
      (is (str/starts-with? xml "<STR0025R1>"))
      (is (str/ends-with? xml "</STR0025R1>"))))
  (testing "response->xml STR0025R1 contains CodMsg STR0025R1"
    (let [fields (str0025/r1-response base-msg nil)
          xml    (str0025/response->xml :STR0025R1 fields)]
      (is (str/includes? xml "<CodMsg>STR0025R1</CodMsg>"))))
  (testing "response->xml STR0025R2 starts and ends with correct tags"
    (let [fields (str0025/r2-response base-msg nil)
          xml    (str0025/response->xml :STR0025R2 fields)]
      (is (str/starts-with? xml "<STR0025R2>"))
      (is (str/ends-with? xml "</STR0025R2>"))))
  (testing "response->xml STR0025E contains CodMsg STR0025E"
    (let [fields (str0025/rejection-response base-msg {:MotivoRejeicao "AC09"})
          xml    (str0025/response->xml :STR0025E fields)]
      (is (str/includes? xml "<CodMsg>STR0025E</CodMsg>")))))
