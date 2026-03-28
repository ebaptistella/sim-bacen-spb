(ns com.github.ebaptistella.logic.str.str0044-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0044 :as str0044]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if   "NC-044"
   :ispb-if-debtd "00000000"
   :dt-movto      "20260327"})

(deftest r1-response-test
  (testing "CodMsg is STR0044R1"
    (is (= :STR0044R1 (:CodMsg (str0044/r1-response base-msg nil)))))
  (testing "NumCtrlIF echoed"
    (is (= "NC-044" (:NumCtrlIF (str0044/r1-response base-msg nil)))))
  (testing "SitLancSTR defaults to LIQUIDADO"
    (is (= "LIQUIDADO" (:SitLancSTR (str0044/r1-response base-msg nil))))))

(deftest response-xml-test
  (testing "R1 wrapping tags"
    (let [xml (str0044/response->xml :STR0044R1 (str0044/r1-response base-msg nil))]
      (is (str/starts-with? xml "<STR0044R1>"))
      (is (str/ends-with? xml "</STR0044R1>")))))
