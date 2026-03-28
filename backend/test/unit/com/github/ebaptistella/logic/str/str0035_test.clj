(ns com.github.ebaptistella.logic.str.str0035-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0035 :as str0035]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if   "NC-035"
   :ispb-if-debtd "00000000"
   :dt-ref        "20260327"})

(deftest r1-response-test
  (testing "XML wraps in STR0035R1"
    (let [xml (str0035/r1-response base-msg)]
      (is (str/starts-with? xml "<STR0035R1>"))
      (is (str/ends-with? xml "</STR0035R1>"))))
  (testing "CodMsg present"
    (is (str/includes? (str0035/r1-response base-msg) "<CodMsg>STR0035R1</CodMsg>")))
  (testing "NumCtrlIF echoed"
    (is (str/includes? (str0035/r1-response base-msg) "<NumCtrlIF>NC-035</NumCtrlIF>")))
  (testing "ISPBIFDebtd echoed"
    (is (str/includes? (str0035/r1-response base-msg) "<ISPBIFDebtd>00000000</ISPBIFDebtd>")))
  (testing "NumCtrlSTR 20 chars"
    (let [xml (str0035/r1-response base-msg)
          num (second (re-find #"<NumCtrlSTR>(.+)</NumCtrlSTR>" xml))]
      (is (= 20 (count num)))))
  (testing "QtdTarif is 0 (simulated empty)"
    (is (str/includes? (str0035/r1-response base-msg) "<QtdTarif>0</QtdTarif>"))))
