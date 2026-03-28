(ns com.github.ebaptistella.logic.str.str0041-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0041 :as str0041]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-041"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "9000.00"
   :finldd-cli     "0013"
   :dt-movto       "20260103"})

(deftest r1-response-test
  (testing "CodMsg is STR0041R1"
    (is (= :STR0041R1 (:CodMsg (str0041/r1-response base-msg nil)))))
  (testing "SitLancSTR defaults to LQDADO"
    (is (= "LQDADO" (:SitLancSTR (str0041/r1-response base-msg nil)))))
  (testing "NumCtrlIF echoed"
    (is (= "NC-041" (:NumCtrlIF (str0041/r1-response base-msg nil)))))
  (testing "NumCtrlSTR 20 chars"
    (is (= 20 (count (:NumCtrlSTR (str0041/r1-response base-msg nil))))))
  (testing "DtMovto echoed"
    (is (= "20260103" (:DtMovto (str0041/r1-response base-msg nil))))))

(deftest r2-response-finldd-cli-test
  (testing "CodMsg is STR0041R2"
    (is (= :STR0041R2 (:CodMsg (str0041/r2-response base-msg nil)))))
  (testing "FinlddCli echoed not FinlddIF"
    (let [fields (str0041/r2-response base-msg nil)]
      (is (= "0013" (:FinlddCli fields)))
      (is (nil? (:FinlddIF fields)))))
  (testing "XML contains FinlddCli"
    (let [xml (str0041/response->xml :STR0041R2 (str0041/r2-response base-msg nil))]
      (is (str/includes? xml "<FinlddCli>0013</FinlddCli>")))))

(deftest rejection-response-sem-motivo-test
  (testing "nil → :missing-motivo"
    (is (= {:error :missing-motivo} (str0041/rejection-response base-msg nil))))
  (testing "{} → :missing-motivo"
    (is (= {:error :missing-motivo} (str0041/rejection-response base-msg {})))))

(deftest rejection-response-com-motivo-test
  (testing "CodMsg is STR0041E"
    (is (= :STR0041E (:CodMsg (str0041/rejection-response base-msg {:MotivoRejeicao "AC09"})))))
  (testing "MotivoRejeicao in XML"
    (let [xml (str0041/response->xml :STR0041E (str0041/rejection-response base-msg {:MotivoRejeicao "AC09"}))]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>")))))

(deftest response-xml-test
  (testing "R1 tags"
    (let [xml (str0041/response->xml :STR0041R1 (str0041/r1-response base-msg nil))]
      (is (str/starts-with? xml "<STR0041R1>"))
      (is (str/ends-with? xml "</STR0041R1>"))))
  (testing "R2 tags"
    (let [xml (str0041/response->xml :STR0041R2 (str0041/r2-response base-msg nil))]
      (is (str/starts-with? xml "<STR0041R2>"))
      (is (str/ends-with? xml "</STR0041R2>")))))
