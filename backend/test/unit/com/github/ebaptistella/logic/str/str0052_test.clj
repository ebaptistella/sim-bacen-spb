(ns com.github.ebaptistella.logic.str.str0052-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0052 :as str0052]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-052"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "12000.00"
   :finldd-if      "00100"
   :dt-movto       "20260103"})

(deftest r1-response-test
  (testing "CodMsg is STR0052R1"
    (is (= :STR0052R1 (:CodMsg (str0052/r1-response base-msg nil)))))
  (testing "SitLancSTR defaults to LQDADO"
    (is (= "LQDADO" (:SitLancSTR (str0052/r1-response base-msg nil)))))
  (testing "NumCtrlSTR 20 chars"
    (is (= 20 (count (:NumCtrlSTR (str0052/r1-response base-msg nil))))))
  (testing "DtMovto echoed"
    (is (= "20260103" (:DtMovto (str0052/r1-response base-msg nil))))))

(deftest r2-response-finldd-if-test
  (testing "CodMsg is STR0052R2"
    (is (= :STR0052R2 (:CodMsg (str0052/r2-response base-msg nil)))))
  (testing "FinlddIF echoed not FinlddCli"
    (let [fields (str0052/r2-response base-msg nil)]
      (is (= "00100" (:FinlddIF fields)))
      (is (nil? (:FinlddCli fields)))))
  (testing "XML contains FinlddIF"
    (let [xml (str0052/response->xml :STR0052R2 (str0052/r2-response base-msg nil))]
      (is (str/includes? xml "<FinlddIF>00100</FinlddIF>"))
      (is (not (str/includes? xml "<FinlddCli>"))))))

(deftest rejection-response-sem-motivo-test
  (testing "nil → :missing-motivo"
    (is (= {:error :missing-motivo} (str0052/rejection-response base-msg nil))))
  (testing "{} → :missing-motivo"
    (is (= {:error :missing-motivo} (str0052/rejection-response base-msg {})))))

(deftest rejection-response-com-motivo-test
  (testing "CodMsg is STR0052E"
    (is (= :STR0052E (:CodMsg (str0052/rejection-response base-msg {:MotivoRejeicao "AC09"})))))
  (testing "MotivoRejeicao in XML"
    (let [xml (str0052/response->xml :STR0052E (str0052/rejection-response base-msg {:MotivoRejeicao "AC09"}))]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>")))))

(deftest response-xml-test
  (testing "R1 tags"
    (let [xml (str0052/response->xml :STR0052R1 (str0052/r1-response base-msg nil))]
      (is (str/starts-with? xml "<STR0052R1>"))
      (is (str/ends-with? xml "</STR0052R1>"))))
  (testing "R2 tags"
    (let [xml (str0052/response->xml :STR0052R2 (str0052/r2-response base-msg nil))]
      (is (str/starts-with? xml "<STR0052R2>"))
      (is (str/ends-with? xml "</STR0052R2>")))))
