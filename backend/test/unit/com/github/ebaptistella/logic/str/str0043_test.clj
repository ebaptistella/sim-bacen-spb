(ns com.github.ebaptistella.logic.str.str0043-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0043 :as str0043]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if   "NC-043"
   :ispb-if-debtd "00000000"
   :dt-movto      "20260327"})

(deftest r1-response-test
  (testing "CodMsg is STR0043R1"
    (is (= :STR0043R1 (:CodMsg (str0043/r1-response base-msg nil)))))
  (testing "NumCtrlIF echoed"
    (is (= "NC-043" (:NumCtrlIF (str0043/r1-response base-msg nil)))))
  (testing "ISPBIFDebtd echoed"
    (is (= "00000000" (:ISPBIFDebtd (str0043/r1-response base-msg nil)))))
  (testing "NumCtrlSTR 20 chars"
    (is (= 20 (count (:NumCtrlSTR (str0043/r1-response base-msg nil))))))
  (testing "SitLancSTR defaults to LIQUIDADO"
    (is (= "LIQUIDADO" (:SitLancSTR (str0043/r1-response base-msg nil)))))
  (testing "SitLancSTR override"
    (is (= "CANCELADO" (:SitLancSTR (str0043/r1-response base-msg {:SitLancSTR "CANCELADO"}))))))

(deftest response-xml-test
  (testing "R1 wrapping tags"
    (let [xml (str0043/response->xml :STR0043R1 (str0043/r1-response base-msg nil))]
      (is (str/starts-with? xml "<STR0043R1>"))
      (is (str/ends-with? xml "</STR0043R1>"))
      (is (str/includes? xml "<NumCtrlIF>NC-043</NumCtrlIF>"))))
  (testing "no R2 or E in field-ordering"
    (is (nil? (str0043/response->xml :STR0043R2 {})))))
