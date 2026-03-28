(ns com.github.ebaptistella.logic.str.str0026-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0026 :as str0026]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-026"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "4000.00"
   :finldd-if      "00026"
   :dt-movto       "20260327"})

(deftest r1-response-test
  (testing "CodMsg and echoed fields"
    (let [r (str0026/r1-response base-msg nil)]
      (is (= :STR0026R1 (:CodMsg r)))
      (is (= "LQDADO" (:SitLancSTR r)))
      (is (= "NC-026" (:NumCtrlIF r))))))

(deftest r2-finldd-if-test
  (testing "R2 uses FinlddIF"
    (let [xml (str0026/response->xml :STR0026R2 (str0026/r2-response base-msg nil))]
      (is (str/includes? xml "<FinlddIF>00026</FinlddIF>"))
      (is (not (str/includes? xml "<FinlddCli>"))))))

(deftest rejection-sem-motivo-test
  (is (= {:error :missing-motivo} (str0026/rejection-response base-msg nil))))
