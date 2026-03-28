(ns com.github.ebaptistella.logic.str.str0040-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0040 :as str0040]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-040"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "10000.00"
   :finldd-if      "00040"
   :dt-movto       "20260327"})

(deftest r1-response-test
  (testing "CodMsg and echoed fields"
    (let [r (str0040/r1-response base-msg nil)]
      (is (= :STR0040R1 (:CodMsg r)))
      (is (= "LQDADO" (:SitLancSTR r)))
      (is (= "NC-040" (:NumCtrlIF r))))))

(deftest r2-finldd-if-test
  (testing "R2 uses FinlddIF"
    (let [xml (str0040/response->xml :STR0040R2 (str0040/r2-response base-msg nil))]
      (is (str/includes? xml "<FinlddIF>00040</FinlddIF>"))
      (is (not (str/includes? xml "<FinlddCli>"))))))

(deftest rejection-sem-motivo-test
  (is (= {:error :missing-motivo} (str0040/rejection-response base-msg nil))))
