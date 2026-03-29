(ns com.github.ebaptistella.wire.in.slb.slb0002-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.wire.in.slb.slb0002 :refer [SLB0002Request]]
            [schema.core :as s]))

(deftest slb0002-schema-validation
  (testing "Valid SLB0002 request with all required fields"
    (is (s/validate SLB0002Request
          {:NumCtrlPart "12345"
           :ISPBPart "00000000"
           :VlrLanc 1000.50})))

  (testing "SLB0002 with optional fields"
    (is (s/validate SLB0002Request
          {:NumCtrlPart "12345"
           :ISPBPart "00000000"
           :VlrLanc 1000.50
           :DtMovto "20260329"
           :Hist "Test payment"})))

  (testing "Invalid: Missing required field NumCtrlPart"
    (is (thrown? Exception
          (s/validate SLB0002Request
            {:ISPBPart "00000000"
             :VlrLanc 1000.50}))))

  (testing "Invalid: Missing required field ISPBPart"
    (is (thrown? Exception
          (s/validate SLB0002Request
            {:NumCtrlPart "12345"
             :VlrLanc 1000.50}))))

  (testing "Invalid: Missing required field VlrLanc"
    (is (thrown? Exception
          (s/validate SLB0002Request
            {:NumCtrlPart "12345"
             :ISPBPart "00000000"}))))

  (testing "Invalid: Negative value for VlrLanc"
    (is (thrown? Exception
          (s/validate SLB0002Request
            {:NumCtrlPart "12345"
             :ISPBPart "00000000"
             :VlrLanc -100}))))

  (testing "Valid: Zero value (edge case)"
    (is (s/validate SLB0002Request
          {:NumCtrlPart "12345"
           :ISPBPart "00000000"
           :VlrLanc 0}))))
