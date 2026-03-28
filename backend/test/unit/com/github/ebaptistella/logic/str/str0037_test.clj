(ns com.github.ebaptistella.logic.str.str0037-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0037 :as str0037]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-037"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "7000.00"
   :finldd-cli     "0016"
   :dt-movto       "20260103"})

(deftest r1-response-test
  (testing "CodMsg is STR0037R1"
    (is (= :STR0037R1 (:CodMsg (str0037/r1-response base-msg nil)))))
  (testing "SitLancSTR defaults to LQDADO"
    (is (= "LQDADO" (:SitLancSTR (str0037/r1-response base-msg nil)))))
  (testing "NumCtrlIF echoed"
    (is (= "NC-037" (:NumCtrlIF (str0037/r1-response base-msg nil)))))
  (testing "NumCtrlSTR 20 chars"
    (is (= 20 (count (:NumCtrlSTR (str0037/r1-response base-msg nil))))))
  (testing "DtMovto echoed"
    (is (= "20260103" (:DtMovto (str0037/r1-response base-msg nil)))))
  (testing "SitLancSTR override"
    (is (= "REJEITADO" (:SitLancSTR (str0037/r1-response base-msg {:SitLancSTR "REJEITADO"}))))))

(deftest r2-response-finldd-cli-test
  (testing "CodMsg is STR0037R2"
    (is (= :STR0037R2 (:CodMsg (str0037/r2-response base-msg nil)))))
  (testing "FinlddCli echoed not FinlddIF"
    (let [fields (str0037/r2-response base-msg nil)]
      (is (= "0016" (:FinlddCli fields)))
      (is (nil? (:FinlddIF fields)))))
  (testing "XML contains FinlddCli"
    (let [xml (str0037/response->xml :STR0037R2 (str0037/r2-response base-msg nil))]
      (is (str/includes? xml "<FinlddCli>0016</FinlddCli>"))
      (is (not (str/includes? xml "<FinlddIF>")))))
  (testing "NumCtrlSTR from params preserved"
    (is (= "abc123fixed00000000x" (:NumCtrlSTR (str0037/r2-response base-msg {:NumCtrlSTR "abc123fixed00000000x"}))))))

(deftest rejection-response-sem-motivo-test
  (testing "nil → :missing-motivo"
    (is (= {:error :missing-motivo} (str0037/rejection-response base-msg nil))))
  (testing "{} → :missing-motivo"
    (is (= {:error :missing-motivo} (str0037/rejection-response base-msg {}))))
  (testing "blank → :missing-motivo"
    (is (= {:error :missing-motivo} (str0037/rejection-response base-msg {:MotivoRejeicao ""})))))

(deftest rejection-response-com-motivo-test
  (testing "CodMsg is STR0037E"
    (is (= :STR0037E (:CodMsg (str0037/rejection-response base-msg {:MotivoRejeicao "AC09"})))))
  (testing "MotivoRejeicao in XML"
    (let [xml (str0037/response->xml :STR0037E (str0037/rejection-response base-msg {:MotivoRejeicao "AC09"}))]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>")))))

(deftest response-xml-test
  (testing "R1 tags"
    (let [xml (str0037/response->xml :STR0037R1 (str0037/r1-response base-msg nil))]
      (is (str/starts-with? xml "<STR0037R1>"))
      (is (str/ends-with? xml "</STR0037R1>"))))
  (testing "R2 tags"
    (let [xml (str0037/response->xml :STR0037R2 (str0037/r2-response base-msg nil))]
      (is (str/starts-with? xml "<STR0037R2>"))
      (is (str/ends-with? xml "</STR0037R2>")))))
