(ns com.github.ebaptistella.logic.str.str0020-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0020 :as str0020]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-020"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "1000.00"
   :finldd-if      "00020"
   :dt-movto       "20260327"})

(deftest r1-response-test
  (testing "CodMsg is STR0020R1"
    (is (= :STR0020R1 (:CodMsg (str0020/r1-response base-msg nil)))))
  (testing "SitLancSTR defaults to LQDADO"
    (is (= "LQDADO" (:SitLancSTR (str0020/r1-response base-msg nil)))))
  (testing "NumCtrlIF echoed"
    (is (= "NC-020" (:NumCtrlIF (str0020/r1-response base-msg nil)))))
  (testing "NumCtrlSTR 20 chars"
    (is (= 20 (count (:NumCtrlSTR (str0020/r1-response base-msg nil))))))
  (testing "DtMovto echoed"
    (is (= "20260327" (:DtMovto (str0020/r1-response base-msg nil)))))
  (testing "SitLancSTR override"
    (is (= "REJEITADO" (:SitLancSTR (str0020/r1-response base-msg {:SitLancSTR "REJEITADO"}))))))

(deftest r2-response-finldd-if-test
  (testing "CodMsg is STR0020R2"
    (is (= :STR0020R2 (:CodMsg (str0020/r2-response base-msg nil)))))
  (testing "FinlddIF echoed not FinlddCli"
    (let [fields (str0020/r2-response base-msg nil)]
      (is (= "00020" (:FinlddIF fields)))
      (is (nil? (:FinlddCli fields)))))
  (testing "XML contains FinlddIF"
    (let [xml (str0020/response->xml :STR0020R2 (str0020/r2-response base-msg nil))]
      (is (str/includes? xml "<FinlddIF>00020</FinlddIF>"))
      (is (not (str/includes? xml "<FinlddCli>"))))))

(deftest rejection-response-sem-motivo-test
  (testing "nil → :missing-motivo"
    (is (= {:error :missing-motivo} (str0020/rejection-response base-msg nil))))
  (testing "blank → :missing-motivo"
    (is (= {:error :missing-motivo} (str0020/rejection-response base-msg {:MotivoRejeicao ""})))))

(deftest rejection-response-com-motivo-test
  (testing "CodMsg is STR0020E"
    (is (= :STR0020E (:CodMsg (str0020/rejection-response base-msg {:MotivoRejeicao "AC09"})))))
  (testing "MotivoRejeicao in XML"
    (let [xml (str0020/response->xml :STR0020E (str0020/rejection-response base-msg {:MotivoRejeicao "AC09"}))]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>")))))

(deftest response-xml-test
  (testing "R1 wrapping tags"
    (let [xml (str0020/response->xml :STR0020R1 (str0020/r1-response base-msg nil))]
      (is (str/starts-with? xml "<STR0020R1>"))
      (is (str/ends-with? xml "</STR0020R1>"))))
  (testing "R2 wrapping tags"
    (let [xml (str0020/response->xml :STR0020R2 (str0020/r2-response base-msg nil))]
      (is (str/starts-with? xml "<STR0020R2>"))
      (is (str/ends-with? xml "</STR0020R2>")))))
