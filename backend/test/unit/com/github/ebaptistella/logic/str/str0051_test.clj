(ns com.github.ebaptistella.logic.str.str0051-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0051 :as str0051]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-051"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "11000.00"
   :finldd-cli     "0017"
   :dt-movto       "20260103"})

(deftest r1-response-test
  (testing "CodMsg is STR0051R1"
    (is (= :STR0051R1 (:CodMsg (str0051/r1-response base-msg nil)))))
  (testing "SitLancSTR defaults to LQDADO"
    (is (= "LQDADO" (:SitLancSTR (str0051/r1-response base-msg nil)))))
  (testing "NumCtrlSTR 20 chars"
    (is (= 20 (count (:NumCtrlSTR (str0051/r1-response base-msg nil))))))
  (testing "DtMovto echoed"
    (is (= "20260103" (:DtMovto (str0051/r1-response base-msg nil))))))

(deftest r2-response-finldd-cli-test
  (testing "CodMsg is STR0051R2"
    (is (= :STR0051R2 (:CodMsg (str0051/r2-response base-msg nil)))))
  (testing "FinlddCli echoed not FinlddIF"
    (let [fields (str0051/r2-response base-msg nil)]
      (is (= "0017" (:FinlddCli fields)))
      (is (nil? (:FinlddIF fields)))))
  (testing "XML contains FinlddCli"
    (let [xml (str0051/response->xml :STR0051R2 (str0051/r2-response base-msg nil))]
      (is (str/includes? xml "<FinlddCli>0017</FinlddCli>")))))

(deftest rejection-response-sem-motivo-test
  (testing "nil → :missing-motivo"
    (is (= {:error :missing-motivo} (str0051/rejection-response base-msg nil))))
  (testing "{} → :missing-motivo"
    (is (= {:error :missing-motivo} (str0051/rejection-response base-msg {})))))

(deftest rejection-response-com-motivo-test
  (testing "CodMsg is STR0051E"
    (is (= :STR0051E (:CodMsg (str0051/rejection-response base-msg {:MotivoRejeicao "AC09"})))))
  (testing "MotivoRejeicao in XML"
    (let [xml (str0051/response->xml :STR0051E (str0051/rejection-response base-msg {:MotivoRejeicao "AC09"}))]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>")))))

(deftest response-xml-test
  (testing "R1 tags"
    (let [xml (str0051/response->xml :STR0051R1 (str0051/r1-response base-msg nil))]
      (is (str/starts-with? xml "<STR0051R1>"))
      (is (str/ends-with? xml "</STR0051R1>"))))
  (testing "R2 tags"
    (let [xml (str0051/response->xml :STR0051R2 (str0051/r2-response base-msg nil))]
      (is (str/starts-with? xml "<STR0051R2>"))
      (is (str/ends-with? xml "</STR0051R2>")))))
