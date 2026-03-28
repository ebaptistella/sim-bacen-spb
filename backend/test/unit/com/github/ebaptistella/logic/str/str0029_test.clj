(ns com.github.ebaptistella.logic.str.str0029-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0029 :as str0029]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-029"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "5000.00"
   :finldd-if      "00029"
   :dt-movto       "20260327"})

(deftest r1-response-test
  (testing "CodMsg and echoed fields"
    (let [r (str0029/r1-response base-msg nil)]
      (is (= :STR0029R1 (:CodMsg r)))
      (is (= "LQDADO" (:SitLancSTR r)))
      (is (= "NC-029" (:NumCtrlIF r))))))

(deftest r2-finldd-if-test
  (testing "R2 uses FinlddIF"
    (let [xml (str0029/response->xml :STR0029R2 (str0029/r2-response base-msg nil))]
      (is (str/includes? xml "<FinlddIF>00029</FinlddIF>"))
      (is (not (str/includes? xml "<FinlddCli>"))))))

(deftest rejection-sem-motivo-test
  (is (= {:error :missing-motivo} (str0029/rejection-response base-msg nil))))
