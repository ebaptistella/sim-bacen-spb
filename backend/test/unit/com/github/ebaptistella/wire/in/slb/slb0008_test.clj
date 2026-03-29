(ns com.github.ebaptistella.wire.in.slb.slb0008-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.wire.in.slb.slb0008 :refer [SLB0008Request]]
            [schema.core :as s]))

(deftest slb0008-schema-validation
  (testing "Valid SLB0008 request with all required fields"
    (is (s/validate SLB0008Request
          {:NumCtrlSLB "22222"
           :ISPBPart "00000000"
           :VlrLanc 750.50})))

  (testing "SLB0008 with optional fields"
    (is (s/validate SLB0008Request
          {:NumCtrlSLB "22222"
           :ISPBPart "00000000"
           :VlrLanc 750.50
           :Hist "Generic debit"
           :DtVenc "20260430"})))

  (testing "Invalid: Missing required field NumCtrlSLB"
    (is (thrown? Exception
          (s/validate SLB0008Request
            {:ISPBPart "00000000"
             :VlrLanc 750.50}))))

  (testing "Invalid: Missing required field ISPBPart"
    (is (thrown? Exception
          (s/validate SLB0008Request
            {:NumCtrlSLB "22222"
             :VlrLanc 750.50}))))

  (testing "Invalid: Missing required field VlrLanc"
    (is (thrown? Exception
          (s/validate SLB0008Request
            {:NumCtrlSLB "22222"
             :ISPBPart "00000000"}))))

  (testing "Invalid: Negative value for VlrLanc"
    (is (thrown? Exception
          (s/validate SLB0008Request
            {:NumCtrlSLB "22222"
             :ISPBPart "00000000"
             :VlrLanc -100}))))

  (testing "Valid: Zero value (edge case)"
    (is (s/validate SLB0008Request
          {:NumCtrlSLB "22222"
           :ISPBPart "00000000"
           :VlrLanc 0})))

  (testing "Valid: With only DtVenc optional field"
    (is (s/validate SLB0008Request
          {:NumCtrlSLB "22222"
           :ISPBPart "00000000"
           :VlrLanc 500
           :DtVenc "20260530"}))))
