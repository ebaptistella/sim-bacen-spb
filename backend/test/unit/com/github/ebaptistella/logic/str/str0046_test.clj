(ns com.github.ebaptistella.logic.str.str0046-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0046 :as str0046]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if     "NC-046"
   :num-ctrl-str-or "NC-020"
   :ispb-if-debtd   "00000000"
   :ispb-if-credtd  "11111111"
   :vlr-lanc        "500.00"
   :finldd-if       "00046"
   :cod-dev-transf  "MD01"
   :dt-movto        "20260327"})

(deftest r1-response-test
  (testing "CodMsg is STR0046R1"
    (is (= :STR0046R1 (:CodMsg (str0046/r1-response base-msg nil)))))
  (testing "NumCtrlIF echoed"
    (is (= "NC-046" (:NumCtrlIF (str0046/r1-response base-msg nil))))))

(deftest r2-includes-num-ctrl-str-or-test
  (testing "CodMsg is STR0046R2"
    (is (= :STR0046R2 (:CodMsg (str0046/r2-response base-msg nil)))))
  (testing "NumCtrlSTROr echoed in R2"
    (let [xml (str0046/response->xml :STR0046R2 (str0046/r2-response base-msg nil))]
      (is (str/includes? xml "<NumCtrlSTROr>NC-020</NumCtrlSTROr>"))))
  (testing "FinlddIF echoed not FinlddCli"
    (let [fields (str0046/r2-response base-msg nil)]
      (is (= "00046" (:FinlddIF fields)))
      (is (nil? (:FinlddCli fields))))))

(deftest rejection-sem-motivo-test
  (is (= {:error :missing-motivo} (str0046/rejection-response base-msg nil))))
