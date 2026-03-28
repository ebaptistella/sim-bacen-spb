(ns com.github.ebaptistella.logic.str.str0004-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0004 :as str0004]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-004"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "9000.00"
   :finldd-if      "00004"
   :dt-movto       "20260327"})

(deftest r1-response-test
  (testing "CodMsg and echoed fields"
    (let [r (str0004/r1-response base-msg nil)]
      (is (= :STR0004R1 (:CodMsg r)))
      (is (= "LQDADO" (:SitLancSTR r)))
      (is (= "NC-004" (:NumCtrlIF r))))))

(deftest r2-finldd-if-test
  (testing "R2 uses FinlddIF"
    (let [xml (str0004/response->xml :STR0004R2 (str0004/r2-response base-msg nil))]
      (is (str/includes? xml "<FinlddIF>00004</FinlddIF>"))
      (is (not (str/includes? xml "<FinlddCli>"))))))

(deftest rejection-sem-motivo-test
  (is (= {:error :missing-motivo} (str0004/rejection-response base-msg nil))))
