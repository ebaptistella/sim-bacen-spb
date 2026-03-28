(ns com.github.ebaptistella.logic.str.str0033-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0033 :as str0033]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-033"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "6000.00"
   :finldd-if      "00033"
   :dt-movto       "20260327"})

(deftest r1-response-test
  (testing "CodMsg and echoed fields"
    (let [r (str0033/r1-response base-msg nil)]
      (is (= :STR0033R1 (:CodMsg r)))
      (is (= "LQDADO" (:SitLancSTR r)))
      (is (= "NC-033" (:NumCtrlIF r))))))

(deftest r2-finldd-if-test
  (testing "R2 uses FinlddIF"
    (let [xml (str0033/response->xml :STR0033R2 (str0033/r2-response base-msg nil))]
      (is (str/includes? xml "<FinlddIF>00033</FinlddIF>"))
      (is (not (str/includes? xml "<FinlddCli>"))))))

(deftest rejection-sem-motivo-test
  (is (= {:error :missing-motivo} (str0033/rejection-response base-msg nil))))
