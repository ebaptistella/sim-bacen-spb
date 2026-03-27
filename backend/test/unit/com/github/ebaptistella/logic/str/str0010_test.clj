(ns com.github.ebaptistella.logic.str.str0010-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0010 :as str0010]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if     "NC-010"
   :num-ctrl-str-or "NC-008"
   :ispb-if-debtd   "00000000"
   :ispb-if-credtd  "11111111"
   :vlr-lanc        "1500.00"
   :cod-dev-transf  "MD06"
   :dt-movto        "20240115"})

(deftest r1-response-test
  (testing "CodMsg is STR0010R1"
    (is (= :STR0010R1 (:CodMsg (str0010/r1-response base-msg nil)))))
  (testing "SitLancSTR defaults to LQDADO"
    (is (= "LQDADO" (:SitLancSTR (str0010/r1-response base-msg nil)))))
  (testing "NumCtrlIF echoed from msg"
    (is (= "NC-010" (:NumCtrlIF (str0010/r1-response base-msg nil)))))
  (testing "ISPBIFDebtd echoed from msg"
    (is (= "00000000" (:ISPBIFDebtd (str0010/r1-response base-msg nil)))))
  (testing "NumCtrlSTR is a 20-char string"
    (let [ncs (:NumCtrlSTR (str0010/r1-response base-msg nil))]
      (is (some? ncs))
      (is (= 20 (count ncs)))))
  (testing "DtMovto echoed from msg"
    (is (= "20240115" (:DtMovto (str0010/r1-response base-msg nil))))))

(deftest r2-response-test
  (testing "CodMsg is STR0010R2"
    (is (= :STR0010R2 (:CodMsg (str0010/r2-response base-msg nil)))))
  (testing "ISPBIFDebtd and ISPBIFCredtd echoed"
    (let [fields (str0010/r2-response base-msg nil)]
      (is (= "00000000" (:ISPBIFDebtd fields)))
      (is (= "11111111" (:ISPBIFCredtd fields)))))
  (testing "VlrLanc echoed"
    (is (= "1500.00" (:VlrLanc (str0010/r2-response base-msg nil)))))
  (testing "CodDevTransf echoed"
    (is (= "MD06" (:CodDevTransf (str0010/r2-response base-msg nil)))))
  (testing "NumCtrlSTROr echoed"
    (is (= "NC-008" (:NumCtrlSTROr (str0010/r2-response base-msg nil)))))
  (testing "NumCtrlSTR from params takes precedence"
    (let [fields (str0010/r2-response base-msg {:NumCtrlSTR "OVERRIDE12345678"})]
      (is (= "OVERRIDE12345678" (:NumCtrlSTR fields))))))

(deftest rejection-response-test
  (testing "nil params → :missing-motivo"
    (is (= {:error :missing-motivo} (str0010/rejection-response base-msg nil))))
  (testing "empty params → :missing-motivo"
    (is (= {:error :missing-motivo} (str0010/rejection-response base-msg {}))))
  (testing "valid MotivoRejeicao → CodMsg STR0010E"
    (let [fields (str0010/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= :STR0010E (:CodMsg fields)))
      (is (= "AC09" (:MotivoRejeicao fields)))))
  (testing "NumCtrlIF and ISPBIFDebtd echoed in rejection"
    (let [fields (str0010/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= "NC-010" (:NumCtrlIF fields)))
      (is (= "00000000" (:ISPBIFDebtd fields))))))

(deftest response->xml-test
  (testing "STR0010R1 XML wraps with correct tags"
    (let [xml (str0010/response->xml :STR0010R1 (str0010/r1-response base-msg nil))]
      (is (str/starts-with? xml "<STR0010R1>"))
      (is (str/ends-with? xml "</STR0010R1>"))
      (is (str/includes? xml "<CodMsg>STR0010R1</CodMsg>"))))
  (testing "STR0010R2 XML includes CodDevTransf"
    (let [xml (str0010/response->xml :STR0010R2 (str0010/r2-response base-msg nil))]
      (is (str/includes? xml "<CodDevTransf>MD06</CodDevTransf>"))))
  (testing "STR0010E XML includes MotivoRejeicao"
    (let [xml (str0010/response->xml :STR0010E (str0010/rejection-response base-msg {:MotivoRejeicao "AC09"}))]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>")))))
