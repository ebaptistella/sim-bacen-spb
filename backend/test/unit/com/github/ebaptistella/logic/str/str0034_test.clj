(ns com.github.ebaptistella.logic.str.str0034-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0034 :as str0034]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-034"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "6000.00"
   :finldd-if      "00006"
   :dt-movto       "20260103"})

(deftest r1-response-test
  (testing "CodMsg is STR0034R1"
    (let [fields (str0034/r1-response base-msg nil)]
      (is (= :STR0034R1 (:CodMsg fields)))))
  (testing "SitLancSTR defaults to LQDADO"
    (let [fields (str0034/r1-response base-msg nil)]
      (is (= "LQDADO" (:SitLancSTR fields)))))
  (testing "NumCtrlIF is echoed from msg"
    (let [fields (str0034/r1-response base-msg nil)]
      (is (= "NC-034" (:NumCtrlIF fields)))))
  (testing "ISPBIFDebtd is echoed from msg"
    (let [fields (str0034/r1-response base-msg nil)]
      (is (= "00000000" (:ISPBIFDebtd fields)))))
  (testing "NumCtrlSTR is present with 20 alphanumeric chars"
    (let [fields   (str0034/r1-response base-msg nil)
          num-ctrl (:NumCtrlSTR fields)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl)))))
  (testing "DtMovto echoes :dt-movto from msg"
    (let [fields (str0034/r1-response base-msg nil)]
      (is (= "20260103" (:DtMovto fields)))))
  (testing "SitLancSTR overridden via params"
    (let [fields (str0034/r1-response base-msg {:SitLancSTR "REJEITADO"})]
      (is (= "REJEITADO" (:SitLancSTR fields))))))

(deftest r2-response-finldd-if-test
  (testing "CodMsg is STR0034R2"
    (let [fields (str0034/r2-response base-msg nil)]
      (is (= :STR0034R2 (:CodMsg fields)))))
  (testing "FinlddIF is echoed from msg (not FinlddCli)"
    (let [fields (str0034/r2-response base-msg nil)]
      (is (= "00006" (:FinlddIF fields)))
      (is (nil? (:FinlddCli fields)))))
  (testing "XML contains FinlddIF and does not contain FinlddCli"
    (let [fields (str0034/r2-response base-msg nil)
          xml    (str0034/response->xml :STR0034R2 fields)]
      (is (str/includes? xml "<FinlddIF>00006</FinlddIF>"))
      (is (not (str/includes? xml "<FinlddCli>")))))
  (testing "NumCtrlSTR from params is preserved"
    (let [fields (str0034/r2-response base-msg {:NumCtrlSTR "abc123fixed00000000x"})]
      (is (= "abc123fixed00000000x" (:NumCtrlSTR fields)))))
  (testing "DtHrBC is present and not blank"
    (let [fields (str0034/r2-response base-msg nil)]
      (is (some? (:DtHrBC fields)))
      (is (not (str/blank? (:DtHrBC fields)))))))

(deftest rejection-response-sem-motivo-test
  (testing "nil params returns :missing-motivo error"
    (is (= {:error :missing-motivo} (str0034/rejection-response base-msg nil))))
  (testing "empty params map returns :missing-motivo error"
    (is (= {:error :missing-motivo} (str0034/rejection-response base-msg {}))))
  (testing "blank MotivoRejeicao returns :missing-motivo error"
    (is (= {:error :missing-motivo} (str0034/rejection-response base-msg {:MotivoRejeicao ""})))))

(deftest rejection-response-com-motivo-test
  (testing "CodMsg is STR0034E"
    (let [fields (str0034/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= :STR0034E (:CodMsg fields)))))
  (testing "MotivoRejeicao appears in XML"
    (let [fields (str0034/rejection-response base-msg {:MotivoRejeicao "AC09"})
          xml    (str0034/response->xml :STR0034E fields)]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>")))))

(deftest response-xml-test
  (testing "R1 tags are correct"
    (let [xml (str0034/response->xml :STR0034R1 (str0034/r1-response base-msg nil))]
      (is (str/starts-with? xml "<STR0034R1>"))
      (is (str/ends-with? xml "</STR0034R1>"))))
  (testing "R2 tags are correct"
    (let [xml (str0034/response->xml :STR0034R2 (str0034/r2-response base-msg nil))]
      (is (str/starts-with? xml "<STR0034R2>"))
      (is (str/ends-with? xml "</STR0034R2>"))))
  (testing "E CodMsg in XML"
    (let [xml (str0034/response->xml :STR0034E (str0034/rejection-response base-msg {:MotivoRejeicao "AC09"}))]
      (is (str/includes? xml "<CodMsg>STR0034E</CodMsg>")))))
